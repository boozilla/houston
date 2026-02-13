package boozilla.houston.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Objects;

@ConfigurationProperties(prefix = "admin.token.allowlist", ignoreUnknownFields = false)
public record AdminTokenAllowlistProperties(
        String inline,
        List<String> tokens,
        Sources sources
) {
    public AdminTokenAllowlistProperties
    {
        inline = Objects.requireNonNullElse(inline, "");
        tokens = List.copyOf(Objects.requireNonNullElse(tokens, List.<String>of()));
        sources = Objects.requireNonNullElseGet(sources, Sources::new);
    }

    public record Sources(
            SecretsManager secretsManager,
            S3 s3,
            SpringConfig springConfig
    ) {
        public Sources
        {
            secretsManager = Objects.requireNonNullElseGet(secretsManager, SecretsManager::new);
            s3 = Objects.requireNonNullElseGet(s3, S3::new);
            springConfig = Objects.requireNonNullElseGet(springConfig, SpringConfig::new);
        }

        public Sources()
        {
            this(new SecretsManager(), new S3(), new SpringConfig());
        }
    }

    public record SecretsManager(
            boolean enabled,
            String secretId,
            String region,
            Long syncIntervalMs
    ) {
        public SecretsManager()
        {
            this(false, null, null, null);
        }
    }

    public record S3(
            boolean enabled,
            String bucket,
            String key,
            String region,
            Long syncIntervalMs
    ) {
        public S3()
        {
            this(false, null, null, null, null);
        }
    }

    public record SpringConfig(
            boolean enabled
    ) {
        public SpringConfig()
        {
            this(false);
        }
    }
}
