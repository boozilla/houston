package boozilla.houston.config;

import boozilla.houston.properties.SshProperties;
import com.jcraft.jsch.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

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

        final var identity = tunnel.ssh().identity();

        if(Objects.nonNull(agentIdentityRepository))
        {
            if(Objects.isNull(identity) || Objects.isNull(identity.name()) || identity.name().isBlank())
            {
                jSch.setIdentityRepository(agentIdentityRepository);
            }

            if(Objects.nonNull(identity) && Objects.nonNull(identity.name()) && !identity.name().isBlank())
            {
                for(final var agentIdentity : agentIdentityRepository.getIdentities())
                {
                    if(agentIdentity.getName().contentEquals(identity.name()))
                    {
                        jSch.addIdentity(agentIdentity, identity.passphrase().getBytes());
                    }
                }
            }
        }

        if(Objects.nonNull(identity) && Objects.nonNull(identity.path()) && !identity.path().isBlank())
        {
            jSch.addIdentity(identity.path());
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
                tunnel.local().hostname(), tunnel.local().port(),
                tunnel.remote().hostname(), tunnel.remote().port()
        );

        if(localPort != tunnel.local().port())
            throw new RuntimeException("Failed to establish tunnel");

        log.info("Local port forwarding: {}:{} --[SSH {}@{}:{}]--> {}:{}",
                tunnel.local().hostname(), tunnel.local().port(),
                session.getUserName(), session.getHost(), session.getPort(),
                tunnel.remote().hostname(), tunnel.remote().port());
    }

    private Session getSession(final SshProperties.Tunnel tunnel)
    {
        final var username = tunnel.ssh().username();
        final var hostname = tunnel.ssh().hostname();
        final var port = tunnel.ssh().port();
        final var password = tunnel.ssh().password();

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
