package boozilla.houston.config;

import boozilla.houston.asset.Assets;
import boozilla.houston.asset.codec.DynamicJsonMarshaller;
import boozilla.houston.decorator.GrpcAuthDecorator;
import boozilla.houston.decorator.ServiceDecorator;
import boozilla.houston.decorator.auth.AdminAuthorizer;
import boozilla.houston.decorator.auth.HttpAuthorizer;
import boozilla.houston.decorator.factory.ScopeDecoratorFactory;
import boozilla.houston.decorator.factory.SecureDecoratorFactory;
import boozilla.houston.properties.GrpcProperties;
import boozilla.houston.unframed.UnframedService;
import com.linecorp.armeria.server.HttpServiceWithRoutes;
import com.linecorp.armeria.server.docs.DocServiceBuilder;
import com.linecorp.armeria.server.grpc.GrpcService;
import com.linecorp.armeria.spring.ArmeriaServerConfigurator;
import io.grpc.BindableService;
import io.grpc.protobuf.services.ProtoReflectionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class RouteConfig {
    @Bean
    public HttpServiceWithRoutes grpcService(final List<BindableService> services,
                                             final GrpcProperties grpcProperties,
                                             final Assets assets)
    {
        final var builder = GrpcService.builder()
                .autoCompression(grpcProperties.autoCompression())
                .useBlockingTaskExecutor(grpcProperties.useBlockingTaskExecutor())
                .jsonMarshallerFactory(serviceDescriptor -> DynamicJsonMarshaller.of(serviceDescriptor, assets))
                .enableUnframedRequests(grpcProperties.enableUnframedRequests())
                .addServices(services);

        if(grpcProperties.enableReflection())
            builder.addService(ProtoReflectionService.newInstance());

        return builder.build();
    }

    @Bean
    public ArmeriaServerConfigurator grpcServiceConfigure(final HttpServiceWithRoutes grpcServices,
                                                          final List<ServiceDecorator> decorators)
    {
        return serverBuilder -> serverBuilder
                .service(grpcServices, decorators);
    }

    @Bean
    public ArmeriaServerConfigurator restServiceConfigure(final List<UnframedService> restServices)
    {
        return serverBuilder -> restServices.forEach(serverBuilder::annotatedService);
    }

    @Bean
    public SecureDecoratorFactory secureDecoratorFactory(final List<HttpAuthorizer> grpcAuthorizer)
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
