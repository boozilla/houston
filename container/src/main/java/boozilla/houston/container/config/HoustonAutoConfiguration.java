package boozilla.houston.container.config;

import boozilla.houston.HoustonChannel;
import boozilla.houston.container.*;
import boozilla.houston.container.interceptor.ManifestInterceptor;
import boozilla.houston.container.interceptor.UpdateInterceptor;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import houston.grpc.service.Manifest;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Set;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(HoustonSettings.class)
@AllArgsConstructor
public class HoustonAutoConfiguration {
    private final HoustonSettings settings;

    @Bean
    @ConditionalOnProperty(name = {"houston.address", "houston.token", "houston.scope"})
    public HoustonChannel houstonChannel()
    {
        return new HoustonChannel(settings.address(),
                settings.token(),
                settings.scope(),
                settings.tls());
    }

    @Bean
    @ConditionalOnBean(HoustonChannel.class)
    public HoustonWatcher houstonWatcher(final HoustonChannel channel,
                                         final Set<UpdateInterceptor<?>> updateInterceptors)
    {
        return new HoustonWatcher(channel, updateInterceptors);
    }

    @Bean
    @ConditionalOnBean(HoustonChannel.class)
    public ManifestLoader manifestHoustonLoader(final HoustonChannel channel)
    {
        return new ManifestHoustonLoader(channel);
    }

    @Bean
    @ConditionalOnBean(ManifestLoader.class)
    public ManifestContainer manifestContainer(final AsyncLoadingCache<String, Manifest> cache,
                                               final ManifestLoader manifestLoader,
                                               final Set<ManifestInterceptor> manifestInterceptors)
    {
        return new ManifestContainer(cache, manifestLoader, manifestInterceptors);
    }

    @Bean
    public Assets assets()
    {
        return new Assets();
    }
}
