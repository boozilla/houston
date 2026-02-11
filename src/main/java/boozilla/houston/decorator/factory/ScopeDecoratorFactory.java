package boozilla.houston.decorator.factory;

import boozilla.houston.HoustonHeaders;
import boozilla.houston.annotation.ScopeService;
import boozilla.houston.asset.Scope;
import boozilla.houston.context.ScopeContext;
import boozilla.houston.decorator.auth.HttpAuthorizer;
import com.linecorp.armeria.server.HttpService;
import com.linecorp.armeria.server.annotation.DecoratorFactoryFunction;
import com.linecorp.armeria.server.auth.AuthService;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public final class ScopeDecoratorFactory implements DecoratorFactoryFunction<@NotNull ScopeService> {
    private final HttpAuthorizer authorizer;

    public ScopeDecoratorFactory(final HttpAuthorizer authorizer)
    {
        this.authorizer = authorizer;
    }

    public Function<? super HttpService, ? extends HttpService> scopeDecorator()
    {
        return AuthService.builder()
                .add(authorizer)
                .onSuccess((delegate, ctx, req) -> {
                    final var scope = req.headers().get(HoustonHeaders.SCOPE, Scope.SERVER.name());
                    ctx.setAttr(ScopeContext.ATTR_SCOPE_KEY, Scope.valueOf(scope));

                    return delegate.serve(ctx, req);
                })
                .onFailure((delegate, ctx, req, error) -> {
                    final var scope = req.headers().get(HoustonHeaders.SCOPE, Scope.CLIENT.name());

                    if(scope.contentEquals(Scope.SERVER.name()))
                    {
                        throw new RuntimeException("Unauthorized access to SERVER scope", error);
                    }

                    ctx.setAttr(ScopeContext.ATTR_SCOPE_KEY, Scope.CLIENT);

                    return delegate.serve(ctx, req);
                })
                .newDecorator();
    }

    @Override
    public @NotNull Function<? super HttpService, ? extends HttpService> newDecorator(final @NotNull ScopeService parameter)
    {
        return scopeDecorator();
    }
}
