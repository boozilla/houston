package boozilla.houston.config;

import com.linecorp.armeria.client.ClientFactory;
import com.linecorp.armeria.common.HttpHeaderNames;
import com.linecorp.armeria.common.TlsKeyPair;
import com.linecorp.armeria.common.util.EventLoopGroups;
import com.linecorp.armeria.common.util.InetAddressPredicates;
import com.linecorp.armeria.server.ClientAddressSource;
import com.linecorp.armeria.server.docs.DocService;
import com.linecorp.armeria.server.docs.DocServiceBuilder;
import com.linecorp.armeria.server.healthcheck.HealthCheckService;
import com.linecorp.armeria.server.logging.AccessLogWriter;
import com.linecorp.armeria.server.logging.LoggingService;
import com.linecorp.armeria.spring.ArmeriaBeanPostProcessor;
import com.linecorp.armeria.spring.ArmeriaServerConfigurator;
import io.micrometer.core.instrument.MeterRegistry;
import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Objects;

@Configuration
public class ArmeriaConfig {
    @Bean
    public static ArmeriaBeanPostProcessor armeriaBeanPostProcessor(final ApplicationContext applicationContext)
    {
        return new ArmeriaBeanPostProcessor(applicationContext.getAutowireCapableBeanFactory());
    }

    @Bean
    public ArmeriaServerConfigurator configure(final AccessLogWriter logger,
                                               final MeterRegistry meterRegistry)
    {
        return serverBuilder -> serverBuilder
                .service("/health", HealthCheckService.of())
                .decorator(LoggingService.newDecorator())
                .accessLogWriter(logger, true)
                .channelOption(ChannelOption.SO_REUSEADDR, true)
                .meterRegistry(meterRegistry);
    }

    @Bean
    @ConditionalOnProperty(name = "armeria.client-address.proxy-cidr")
    public ArmeriaServerConfigurator clientAddressSource(@Value("${armeria.client-address.proxy-cidr}") final String proxyCidr)
    {
        return serverBuilder -> serverBuilder.clientAddressTrustedProxyFilter(InetAddressPredicates.ofCidr(proxyCidr))
                .clientAddressSources(ClientAddressSource.ofHeader(HttpHeaderNames.X_FORWARDED_FOR),
                        ClientAddressSource.ofHeader(HttpHeaderNames.FORWARDED),
                        ClientAddressSource.ofProxyProtocol());
    }

    @Bean
    public ArmeriaServerConfigurator tls(@Nullable @Value("${armeria.tls.private-key-file:#{null}}") final String privateKeyFile,
                                         @Nullable @Value("${armeria.tls.cert-chain-file:#{null}}") final String certChainFile)
    {
        return serverBuilder -> {
            if(Objects.nonNull(privateKeyFile) && Objects.nonNull(certChainFile))
            {
                serverBuilder.tls(TlsKeyPair.of(new File(privateKeyFile), new File(certChainFile)));
            }
            else
            {
                serverBuilder.tlsSelfSigned();
            }
        };
    }

    @Bean
    public AccessLogWriter accessLogWriter()
    {
        final var delegate = AccessLogWriter.common();

        return log -> {
            final var path = log.requestHeaders().path();

            if(path.startsWith("/grpc.health.v1.Health/"))
                return;

            delegate.log(log);
        };
    }

    @Bean(destroyMethod = "closeAsync")
    public ClientFactory clientFactory()
    {
        return ClientFactory.builder()
                .workerGroup(EventLoopGroups.newEventLoopGroup(Runtime.getRuntime().availableProcessors()), true)
                .build();
    }

    @Bean
    @Profile("docs")
    public DocServiceBuilder docServiceBuilder()
    {
        return DocService.builder();
    }
}
