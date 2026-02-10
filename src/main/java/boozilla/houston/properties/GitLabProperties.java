package boozilla.houston.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("gitlab")
public record GitLabProperties(
        String url,
        String accessToken
) {
}
