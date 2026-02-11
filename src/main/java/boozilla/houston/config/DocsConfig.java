package boozilla.houston.config;

import boozilla.houston.HoustonHeaders;
import boozilla.houston.asset.Scope;
import com.linecorp.armeria.common.HttpHeaders;
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

    @Override
    public void configure(final ServerBuilder serverBuilder)
    {
        serverBuilder.serviceUnder("/docs", docServiceBuilder
                .exampleHeaders(exampleHeaders())
                .exclude(DocServiceFilter.ofGrpc()
                        .and(DocServiceFilter.ofServiceName("grpc.reflection.v1.ServerReflection")))
                .build());
    }

    private HttpHeaders[] exampleHeaders()
    {
        return new HttpHeaders[] {
                // Client scope request
                HttpHeaders.builder()
                        .add(HoustonHeaders.TOKEN, "")
                        .add(HoustonHeaders.SCOPE, Scope.CLIENT.name())
                        .build(),

                // Server scope request
                HttpHeaders.builder()
                        .add(HoustonHeaders.TOKEN, "")
                        .add(HoustonHeaders.SCOPE, Scope.SERVER.name())
                        .build(),

                // Secured service request
                HttpHeaders.builder()
                        .add(HoustonHeaders.TOKEN, "")
                        .build()
        };
    }
}
