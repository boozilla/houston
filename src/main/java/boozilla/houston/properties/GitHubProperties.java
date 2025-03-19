package boozilla.houston.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("github")
public record GitHubProperties(
        String accessToken,
        String webhookSecret
) {

}
