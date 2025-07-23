package boozilla.houston.container.config;

import boozilla.houston.HoustonChannel;
import boozilla.houston.asset.Scope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "houston", name = "enabled", havingValue = "true")
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
    public HoustonChannel getChannel()
    {
        return new HoustonChannel(address, token, scope, tls);
    }
}
