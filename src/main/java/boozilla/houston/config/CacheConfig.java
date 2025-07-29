package boozilla.houston.config;

import boozilla.houston.container.ManifestLoader;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import houston.grpc.service.Manifest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CacheConfig {
    @Bean
    public AsyncLoadingCache<String, Manifest> manifestCache(final ManifestLoader manifestLoader)
    {
        return Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofHours(1))
                .buildAsync(manifestLoader);
    }
}
