package boozilla.houston.config;

import boozilla.houston.properties.SshProperties;
import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Configuration
public class SshTunnelConfig implements AutoCloseable {
    private final SshProperties sshProperties;
    private final AgentIdentityRepository agentIdentityRepository;
    private final List<Session> sessions;

    public SshTunnelConfig(final SshProperties sshProperties) throws AgentProxyException, JSchException
    {
        this.sshProperties = sshProperties;
        this.sessions = new ArrayList<>();

        final var agentConnector = getAgentConnector(sshProperties.agentSock());
        this.agentIdentityRepository = getIdentityRepository(agentConnector);
    }

    private JSch getJsch(final SshProperties.Tunnel tunnel) throws JSchException
    {
        final var jSch = new JSch();
        jSch.setKnownHosts(sshProperties.knownHosts());

        if(Objects.nonNull(agentIdentityRepository))
        {
            if(Objects.isNull(tunnel.sshIdentityName()) || tunnel.sshIdentityName().isBlank())
            {
                jSch.setIdentityRepository(agentIdentityRepository);
            }

            if(Objects.nonNull(tunnel.sshIdentityName()) && !tunnel.sshIdentityName().isBlank())
            {
                for(final var identity : agentIdentityRepository.getIdentities())
                {
                    if(identity.getName().contentEquals(tunnel.sshIdentityName()))
                    {
                        jSch.addIdentity(identity, tunnel.sshIdentityPassphrase().getBytes());
                    }
                }
            }
        }

        if(Objects.nonNull(tunnel.sshIdentityPath()) && !tunnel.sshIdentityPath().isBlank())
        {
            jSch.addIdentity(tunnel.sshIdentityPath());
        }

        return jSch;
    }

    private AgentConnector getAgentConnector(final String agentSock) throws AgentProxyException
    {
        if(Objects.nonNull(agentSock))
            return new SSHAgentConnector(Path.of(agentSock));

        if(Objects.nonNull(System.getenv("SSH_AUTH_SOCK")))
            return new SSHAgentConnector();

        return null;
    }

    private AgentIdentityRepository getIdentityRepository(final AgentConnector connector)
    {
        return new AgentIdentityRepository(connector);
    }

    @PostConstruct
    public void establishTunnels() throws JSchException
    {
        for(final var tunnel : sshProperties.tunnels())
        {
            final var session = getSession(tunnel);

            establishTunnel(session, tunnel);
        }
    }

    private void establishTunnel(final Session session, final SshProperties.Tunnel tunnel) throws JSchException
    {
        final var localPort = session.setPortForwardingL(
                tunnel.localHostname(), tunnel.localPort(),
                tunnel.remoteHostname(), tunnel.remotePort()
        );

        if(localPort != tunnel.localPort())
            throw new RuntimeException("Failed to establish tunnel");

        log.info("Local port forwarding: {}:{} --[SSH {}@{}:{}]--> {}:{}",
                tunnel.localHostname(), tunnel.localPort(),
                session.getUserName(), session.getHost(), session.getPort(),
                tunnel.remoteHostname(), tunnel.remotePort());
    }

    private Session getSession(final SshProperties.Tunnel tunnel)
    {
        final var username = tunnel.sshUsername();
        final var hostname = tunnel.sshHostname();
        final var port = tunnel.sshPort();
        final var password = tunnel.sshPassword();

        try
        {
            final var session = getJsch(tunnel).getSession(username, hostname, port);
            sessions.add(session);

            if(Objects.nonNull(password))
            {
                session.setConfig("PreferredAuthentications", "publickey,password,keyboard-interactive");
                session.setPassword(password);
            }
            else
            {
                session.setConfig("PreferredAuthentications", "publickey");
            }

            log.info("Connecting SSH {}@{}:{}", username, hostname, port);
            session.connect();
            log.info("SSH connected {}@{}:{}", username, hostname, port);

            return session;
        }
        catch(JSchException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    @PreDestroy
    public void close()
    {
        sessions.forEach(Session::disconnect);
    }
}
