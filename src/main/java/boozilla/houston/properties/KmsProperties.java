package boozilla.houston.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("kms")
public record KmsProperties(
        String adminKey
) {

}
