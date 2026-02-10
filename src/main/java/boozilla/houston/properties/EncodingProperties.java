package boozilla.houston.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties("armeria.encoding")
public record EncodingProperties(
        boolean enabled,
        List<String> algorithms
) {

}
