package boozilla.houston.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("armeria.mcp")
public record McpProperties(
        boolean enabled,
        String path,
        String name,
        String version
) {

}
