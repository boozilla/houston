package boozilla.houston.decorator.factory;

import boozilla.houston.annotation.ScopeService;
import boozilla.houston.asset.Scope;
import boozilla.houston.context.ScopeContext;
import boozilla.houston.decorator.auth.GrpcAuthorizer;
import com.linecorp.armeria.common.HttpHeaders;
import com.linecorp.armeria.server.HttpService;
import com.linecorp.armeria.server.annotation.DecoratorFactoryFunction;
import com.linecorp.armeria.server.auth.AuthService;
import com.linecorp.armeria.server.docs.DocServiceBuilder;
import org.jetbrains.annotations.NotNull;
import org.springframework.lang.Nullable;

import java.util.Objects;
import java.util.function.Function;

public final class ScopeDecoratorFactory implements DecoratorFactoryFunction<@NotNull ScopeService> {
    private static final String SCOPE_HEADER_NAME = "x-houston-scope";

    private final GrpcAuthorizer grpcAuthorizer;

    public ScopeDecoratorFactory(@Nullable final DocServiceBuilder docServiceBuilder,
                                 final GrpcAuthorizer grpcAuthorizer)
    {
        if(Objects.nonNull(docServiceBuilder))
            docServiceBuilder.exampleHeaders(HttpHeaders.of(SCOPE_HEADER_NAME, Scope.CLIENT.name()));

        this.grpcAuthorizer = grpcAuthorizer;
    }

    @Override
    public @NotNull Function<? super HttpService, ? extends HttpService> newDecorator(final @NotNull ScopeService parameter)
    {
        return AuthService.builder()
                .add(grpcAuthorizer)
                .onSuccess((delegate, ctx, req) -> {
                    final var scope = req.headers().get(SCOPE_HEADER_NAME, Scope.SERVER.name());
                    ctx.setAttr(ScopeContext.ATTR_SCOPE_KEY, Scope.valueOf(scope));

                    return delegate.serve(ctx, req);
                })
                .onFailure((delegate, ctx, req, error) -> {
                    ctx.setAttr(ScopeContext.ATTR_SCOPE_KEY, Scope.CLIENT);

                    return delegate.serve(ctx, req);
                })
                .newDecorator();
    }
}