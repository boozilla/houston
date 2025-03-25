package boozilla.houston.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("key.kms")
public record KmsProperties(
        String id
) {

}
