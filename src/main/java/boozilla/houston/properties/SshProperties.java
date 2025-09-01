package boozilla.houston.properties;

import org.apache.logging.log4j.util.Strings;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Objects;
import java.util.Set;

@ConfigurationProperties("ssh")
public record SshProperties(
        String knownHosts,
        String agentSock,
        Set<Tunnel> tunnels
) {
    public record Tunnel(
            String sshHostname,
            int sshPort,
            String sshUsername,
            String sshPassword,
            String sshIdentityName,
            String sshIdentityPassphrase,
            String sshIdentityPath,
            String localHostname,
            int localPort,
            String remoteHostname,
            int remotePort
    ) {
        public String localHostname()
        {
            return Objects.requireNonNullElse(localHostname, "127.0.0.1");
        }

        public String sshIdentityPassphrase()
        {
            return Objects.requireNonNullElse(sshIdentityPassphrase, Strings.EMPTY);
        }
    }

    public String knownHosts()
    {
        return Objects.requireNonNullElse(knownHosts, "~/.ssh/known_hosts");
    }

    public Set<Tunnel> tunnels()
    {
        return Objects.requireNonNullElse(tunnels, Set.of());
    }
}
