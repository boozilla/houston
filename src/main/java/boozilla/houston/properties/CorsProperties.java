package boozilla.houston.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties("armeria.cors")
public record CorsProperties(
        List<String> origins
) {

}
