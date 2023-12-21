package boozilla.houston.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import software.amazon.awssdk.regions.Region;

import java.net.URI;

@ConfigurationProperties("aws")
public record AwsProperties(
        URI endpoint,
        String accessKey,
        String secretKey,
        Region region
) {

}
