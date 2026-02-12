package boozilla.houston.config;

import com.linecorp.armeria.client.HttpClient;
import com.linecorp.armeria.client.retry.Backoff;
import com.linecorp.armeria.client.retry.RetryRule;
import com.linecorp.armeria.client.retry.RetryingClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

@Configuration
public class RetryConfig {
    @Bean
    public Function<? super HttpClient, RetryingClient> retryDecorator()
    {
        return RetryingClient.builder(RetryRule.failsafe(Backoff.ofDefault()))
                .maxTotalAttempts(3)
                .newDecorator();
    }
}
