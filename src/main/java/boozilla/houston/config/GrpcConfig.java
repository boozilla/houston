package boozilla.houston.config;

import boozilla.houston.decorator.GrpcAuthDecorator;
import boozilla.houston.decorator.auth.AdminAuthorizer;
import boozilla.houston.decorator.auth.GrpcAuthorizer;
import boozilla.houston.decorator.factory.ScopeDecoratorFactory;
import boozilla.houston.decorator.factory.SecureDecoratorFactory;
import boozilla.houston.properties.GrpcProperties;
import com.linecorp.armeria.server.HttpServiceWithRoutes;
import com.linecorp.armeria.server.docs.DocServiceBuilder;
import com.linecorp.armeria.server.grpc.GrpcService;
import io.grpc.BindableService;
import io.grpc.protobuf.services.ProtoReflectionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class GrpcConfig {
    @Bean
    public HttpServiceWithRoutes grpcService(final List<BindableService> services, final GrpcProperties grpcProperties)
    {
        final var builder = GrpcService.builder()
                .useBlockingTaskExecutor(grpcProperties.useBlockingTaskExecutor())
                .enableUnframedRequests(grpcProperties.enableUnframedRequests())
                .addServices(services);

        if(grpcProperties.enableReflection())
            builder.addService(ProtoReflectionService.newInstance());

        return builder.build();
    }

    @Bean
    public SecureDecoratorFactory secureDecoratorFactory(final List<GrpcAuthorizer> grpcAuthorizer)
    {
        return new SecureDecoratorFactory(new GrpcAuthDecorator(grpcAuthorizer));
    }

    @Bean
    public ScopeDecoratorFactory scopeDecoratorFactory(final DocServiceBuilder docServiceBuilder,
                                                       final AdminAuthorizer adminAuthorizer)
    {
        return new ScopeDecoratorFactory(docServiceBuilder, adminAuthorizer);
    }
}
