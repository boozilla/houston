package boozilla.houston.decorator;

import boozilla.houston.properties.CorsProperties;
import com.linecorp.armeria.common.HttpHeaderNames;
import com.linecorp.armeria.common.HttpMethod;
import com.linecorp.armeria.common.grpc.protocol.GrpcHeaderNames;
import com.linecorp.armeria.server.HttpService;
import com.linecorp.armeria.server.cors.CorsService;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class CorsDecorator implements ServiceDecorator {
    private final Function<? super HttpService, CorsService> service;

    public CorsDecorator(final CorsProperties corsProperties)
    {
        service = CorsService.builder(corsProperties.origins())
                .allowRequestMethods(HttpMethod.POST)
                .allowRequestHeaders(HttpHeaderNames.CONTENT_TYPE, HttpHeaderNames.of("X-GRPC-WEB"))
                .exposeHeaders(GrpcHeaderNames.GRPC_STATUS,
                        GrpcHeaderNames.GRPC_MESSAGE,
                        GrpcHeaderNames.ARMERIA_GRPC_THROWABLEPROTO_BIN)
                .newDecorator();
    }

    @Override
    public HttpService apply(final HttpService httpService)
    {
        return service.apply(httpService);
    }
}
