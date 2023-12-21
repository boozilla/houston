package boozilla.houston.config;

import boozilla.houston.decorator.ServiceDecorator;
import com.linecorp.armeria.common.HttpHeaderNames;
import com.linecorp.armeria.server.ClientAddressSource;
import com.linecorp.armeria.server.HttpServiceWithRoutes;
import com.linecorp.armeria.server.docs.DocService;
import com.linecorp.armeria.server.docs.DocServiceBuilder;
import com.linecorp.armeria.server.healthcheck.HealthCheckService;
import com.linecorp.armeria.server.logging.AccessLogWriter;
import com.linecorp.armeria.spring.ArmeriaServerConfigurator;
import io.micrometer.core.instrument.MeterRegistry;
import io.netty.channel.ChannelOption;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Duration;
import java.util.List;

@Configuration
public class ArmeriaConfig {
    @Bean
    public ArmeriaServerConfigurator configure(final AccessLogWriter logger,
                                               final HttpServiceWithRoutes grpcServices,
                                               final List<ServiceDecorator> decorators,
                                               final MeterRegistry meterRegistry)
    {
        return serverBuilder -> serverBuilder.service(grpcServices, decorators)
                .service("/health", HealthCheckService.of())
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
