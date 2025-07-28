package boozilla.houston.container.config;

import boozilla.houston.HoustonChannel;
import boozilla.houston.container.HoustonWatcher;
import boozilla.houston.container.interceptor.ManifestInterceptor;
import boozilla.houston.container.interceptor.UpdateInterceptor;
import lombok.AllArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Objects;
import java.util.Set;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(HoustonSettings.class)
@AllArgsConstructor
public class HoustonAutoConfiguration {
    private final HoustonSettings settings;

    @Bean
    public HoustonChannel houstonChannel()
    {
        return new HoustonChannel(settings.address(),
                settings.token(),
                settings.scope(),
                settings.tls());
    }

    @Bean
    public HoustonWatcher houstonWatcher(final HoustonChannel channel,
                                         final Set<ManifestInterceptor> manifestInterceptors,
                                         final Set<UpdateInterceptor<?>> updateInterceptors)
    {
        return new HoustonWatcher(channel,
                Objects.requireNonNullElse(settings.manifests(), Set.of()),
                manifestInterceptors,
                updateInterceptors);
    }
}
