package boozilla.houston.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Objects;

@ConfigurationProperties(prefix = "admin.token.allowlist", ignoreUnknownFields = false)
public record AdminTokenAllowlistProperties(
        List<String> tokens,
        Sources sources
) {
    public AdminTokenAllowlistProperties
    {
        tokens = List.copyOf(Objects.requireNonNullElse(tokens, List.<String>of()));
        sources = Objects.requireNonNullElseGet(sources, Sources::new);
    }

    public static class Sources {
        private SecretsManager secretsManager = new SecretsManager();
        private S3 s3 = new S3();
        private SpringConfig springConfig = new SpringConfig();

        public SecretsManager getSecretsManager()
        {
            return secretsManager;
        }

        public void setSecretsManager(final SecretsManager secretsManager)
        {
            this.secretsManager = Objects.requireNonNullElseGet(secretsManager, SecretsManager::new);
        }

        public S3 getS3()
        {
            return s3;
        }

        public void setS3(final S3 s3)
        {
            this.s3 = Objects.requireNonNullElseGet(s3, S3::new);
        }

        public SpringConfig getSpringConfig()
        {
            return springConfig;
        }

        public void setSpringConfig(final SpringConfig springConfig)
        {
            this.springConfig = Objects.requireNonNullElseGet(springConfig, SpringConfig::new);
        }

        public SecretsManager secretsManager()
        {
            return secretsManager;
        }

        public S3 s3()
        {
            return s3;
        }

        public SpringConfig springConfig()
        {
            return springConfig;
        }
    }

    public static class SecretsManager {
        private boolean enabled;
        private String secretId;
        private String region;
        private Long syncIntervalMs;

        public boolean isEnabled()
        {
            return enabled;
        }

        public void setEnabled(final boolean enabled)
        {
            this.enabled = enabled;
        }

        public String getSecretId()
        {
            return secretId;
        }

        public void setSecretId(final String secretId)
        {
            this.secretId = secretId;
        }

        public String getRegion()
        {
            return region;
        }

        public void setRegion(final String region)
        {
            this.region = region;
        }

        public Long getSyncIntervalMs()
        {
            return syncIntervalMs;
        }

        public void setSyncIntervalMs(final Long syncIntervalMs)
        {
            this.syncIntervalMs = syncIntervalMs;
        }

        public boolean enabled()
        {
            return enabled;
        }

        public String secretId()
        {
            return secretId;
        }

        public String region()
        {
            return region;
        }

        public Long syncIntervalMs()
        {
            return syncIntervalMs;
        }
    }

    public static class S3 {
        private boolean enabled;
        private String bucket;
        private String key;
        private String region;
        private Long syncIntervalMs;

        public boolean isEnabled()
        {
            return enabled;
        }

        public void setEnabled(final boolean enabled)
        {
            this.enabled = enabled;
        }

        public String getBucket()
        {
            return bucket;
        }

        public void setBucket(final String bucket)
        {
            this.bucket = bucket;
        }

        public String getKey()
        {
            return key;
        }

        public void setKey(final String key)
        {
            this.key = key;
        }

        public String getRegion()
        {
            return region;
        }

        public void setRegion(final String region)
        {
            this.region = region;
        }

        public Long getSyncIntervalMs()
        {
            return syncIntervalMs;
        }

        public void setSyncIntervalMs(final Long syncIntervalMs)
        {
            this.syncIntervalMs = syncIntervalMs;
        }

        public boolean enabled()
        {
            return enabled;
        }

        public String bucket()
        {
            return bucket;
        }

        public String key()
        {
            return key;
        }

        public String region()
        {
            return region;
        }

        public Long syncIntervalMs()
        {
            return syncIntervalMs;
        }
    }

    public static class SpringConfig {
        private boolean enabled;

        public boolean isEnabled()
        {
            return enabled;
        }

        public void setEnabled(final boolean enabled)
        {
            this.enabled = enabled;
        }

        public boolean enabled()
        {
            return enabled;
        }
    }
}
