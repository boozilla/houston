package boozilla.houston.config;

import boozilla.houston.asset.Scope;
import boozilla.houston.context.ScopeContext;
import boozilla.houston.decorator.auth.JwtAdminAuthorizer;
import boozilla.houston.mcp.McpResourceProvider;
import boozilla.houston.mcp.McpToolProvider;
import boozilla.houston.properties.McpProperties;
import com.linecorp.armeria.server.ai.mcp.ArmeriaStreamableServerTransportProvider;
import com.linecorp.armeria.server.auth.AuthService;
import com.linecorp.armeria.spring.ArmeriaServerConfigurator;
import io.modelcontextprotocol.server.McpAsyncServer;
import io.modelcontextprotocol.server.McpServer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConditionalOnProperty(name = "armeria.mcp.enabled", havingValue = "true")
public class McpConfig {
    private static final String SCOPE_HEADER_NAME = "x-houston-scope";

    @Bean
    public ArmeriaStreamableServerTransportProvider mcpTransport()
    {
        return ArmeriaStreamableServerTransportProvider.builder()
                .build();
    }

    @Bean(destroyMethod = "close")
    public McpAsyncServer mcpServer(final ArmeriaStreamableServerTransportProvider transport,
                                    final McpProperties mcpProperties,
                                    final List<McpToolProvider> toolProviders,
                                    final List<McpResourceProvider> resourceProviders)
    {
        final var builder = McpServer.async(transport)
                .serverInfo(mcpProperties.name(), mcpProperties.version());

        toolProviders.stream()
                .flatMap(provider -> provider.tools().stream())
                .forEach(builder::tools);

        resourceProviders.stream()
                .flatMap(provider -> provider.resources().stream())
                .forEach(builder::resources);

        return builder.build();
    }

    @Bean
    public ArmeriaServerConfigurator mcpServiceConfigure(
            final ArmeriaStreamableServerTransportProvider transport,
            final McpProperties mcpProperties,
            final JwtAdminAuthorizer adminAuthorizer)
    {
        return serverBuilder -> {
            final var authDecorator = AuthService.builder()
                    .add(adminAuthorizer)
                    .onSuccess((delegate, ctx, req) -> {
                        final var scopeHeader = req.headers().get(SCOPE_HEADER_NAME, Scope.SERVER.name());
                        try
                        {
                            ctx.setAttr(ScopeContext.ATTR_SCOPE_KEY, Scope.valueOf(scopeHeader));
                        }
                        catch(IllegalArgumentException e)
                        {
                            ctx.setAttr(ScopeContext.ATTR_SCOPE_KEY, Scope.CLIENT);
                        }
                        return delegate.serve(ctx, req);
                    })
                    .onFailure((delegate, ctx, req, error) -> {
                        ctx.setAttr(ScopeContext.ATTR_SCOPE_KEY, Scope.CLIENT);
                        return delegate.serve(ctx, req);
                    })
                    .newDecorator();

            serverBuilder.serviceUnder(mcpProperties.path(),
                    transport.httpService().decorate(authDecorator));
        };
    }
}
