package boozilla.houston.config;

import com.linecorp.armeria.common.HttpHeadersBuilder;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.docs.DocServiceBuilder;
import com.linecorp.armeria.server.docs.DocServiceFilter;
import com.linecorp.armeria.spring.ArmeriaServerConfigurator;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("docs")
@Configuration
@AllArgsConstructor
public class DocsConfig implements ArmeriaServerConfigurator {
    private final DocServiceBuilder docServiceBuilder;
    private final HttpHeadersBuilder exampleHeaders;

    @Override
    public void configure(final ServerBuilder serverBuilder)
    {
        serverBuilder.serviceUnder("/docs", docServiceBuilder
                .exampleHeaders(exampleHeaders.build())
                .exclude(DocServiceFilter.ofGrpc()
                        .and(DocServiceFilter.ofServiceName("grpc.reflection.v1.ServerReflection")))
                .build());
    }
}
