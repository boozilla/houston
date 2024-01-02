package boozilla.houston.config;

import com.linecorp.armeria.common.HttpHeaderNames;
import com.linecorp.armeria.server.ClientAddressSource;
import com.linecorp.armeria.server.docs.DocService;
import com.linecorp.armeria.server.docs.DocServiceBuilder;
import com.linecorp.armeria.server.healthcheck.HealthCheckService;
import com.linecorp.armeria.server.logging.AccessLogWriter;
import com.linecorp.armeria.server.logging.LoggingService;
import com.linecorp.armeria.spring.ArmeriaServerConfigurator;
import io.micrometer.core.instrument.MeterRegistry;
import io.netty.channel.ChannelOption;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Duration;

@Configuration
public class ArmeriaConfig {
    @Bean
    public ArmeriaServerConfigurator configure(final AccessLogWriter logger,
                                               final MeterRegistry meterRegistry)
    {
        return serverBuilder -> serverBuilder
                .service("/health", HealthCheckService.of())
                .decorator(LoggingService.newDecorator())
                .accessLogWriter(logger, true)
                .channelOption(ChannelOption.SO_REUSEADDR, true)
                .clientAddressSources(ClientAddressSource.ofHeader(HttpHeaderNames.X_FORWARDED_FOR))
                .meterRegistry(meterRegistry)
                .gracefulShutdownTimeout(Duration.ofSeconds(3), Duration.ofSeconds(10));
    }

    @Bean
    public AccessLogWriter accessLogWriter()
    {
        return AccessLogWriter.common();
    }

    @Bean
    @Profile("docs")
    public DocServiceBuilder docServiceBuilder()
    {
        return DocService.builder();
    }
}
