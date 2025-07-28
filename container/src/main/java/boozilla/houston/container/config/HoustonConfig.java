package boozilla.houston.container.config;

import boozilla.houston.HoustonChannel;
import boozilla.houston.asset.Scope;
import boozilla.houston.container.HoustonWatcher;
import boozilla.houston.container.interceptor.UpdateInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Set;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = {"houston.address", "houston.token", "houston.scope", "houston.tls"})
public class HoustonConfig {
    final String address;
    final String token;
    final Scope scope;
    final boolean tls;

    public HoustonConfig(@Value("${houston.address}") final String address,
                         @Value("${houston.token}") final String token,
                         @Value("${houston.scope}") final Scope scope,
                         @Value("${houston.tls}") final boolean tls)
    {
        this.address = address;
        this.token = token;
        this.scope = scope;
        this.tls = tls;
    }

    @Bean
    public HoustonChannel houstonChannel()
    {
        return new HoustonChannel(address, token, scope, tls);
    }

    @Bean
    public HoustonWatcher houstonWatcher(final HoustonChannel channel, final Set<UpdateInterceptor<?>> interceptors)
    {
        return new HoustonWatcher(channel, interceptors);
    }
}
