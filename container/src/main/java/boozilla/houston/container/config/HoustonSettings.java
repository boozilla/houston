package boozilla.houston.container.config;

import boozilla.houston.asset.Scope;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@ConfigurationProperties(prefix = "houston")
public record HoustonSettings(
        String address,
        String token,
        Scope scope,
        boolean tls,
        Set<String> manifests
) {
}
