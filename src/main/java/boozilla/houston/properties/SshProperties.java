package boozilla.houston.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Objects;
import java.util.Set;

@ConfigurationProperties("ssh")
public record SshProperties(
        String knownHosts,
        String agentSock,
        Set<Tunnel> tunnels
) {
    public String knownHosts()
    {
        return Objects.requireNonNullElse(knownHosts, "~/.ssh/known_hosts");
    }

    public Set<Tunnel> tunnels()
    {
        return Objects.requireNonNullElse(tunnels, Set.of());
    }

    public record Tunnel(
            Ssh ssh,
            Local local,
            Remote remote
    ) {
        public record Ssh(
                String hostname,
                int port,
                String username,
                String password,
                Identity identity
        ) {
            public record Identity(
                    String name,
                    String passphrase,
                    String path
            ) {
                public String passphrase()
                {
                    return Objects.requireNonNullElse(passphrase, "");
                }
            }
        }

        public record Local(
                String hostname,
                int port
        ) {
            public String hostname()
            {
                return Objects.requireNonNullElse(hostname, "127.0.0.1");
            }
        }

        public record Remote(
                String hostname,
                int port
        ) {
        }
    }
}
