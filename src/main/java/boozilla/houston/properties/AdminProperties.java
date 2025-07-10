package boozilla.houston.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties("admin")
public record AdminProperties(
        List<String> hosts
) {

}
