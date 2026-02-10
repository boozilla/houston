package boozilla.houston.decorator.factory;

import boozilla.houston.annotation.ScopeService;
import boozilla.houston.asset.Scope;
import boozilla.houston.context.ScopeContext;
import boozilla.houston.decorator.auth.HttpAuthorizer;
import com.linecorp.armeria.common.HttpHeadersBuilder;
import com.linecorp.armeria.server.HttpService;
import com.linecorp.armeria.server.annotation.DecoratorFactoryFunction;
import com.linecorp.armeria.server.auth.AuthService;
import jakarta.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;

public final class ScopeDecoratorFactory implements DecoratorFactoryFunction<@NotNull ScopeService> {
    private static final String SCOPE_HEADER_NAME = "x-houston-scope";

    private final HttpAuthorizer authorizer;

    public ScopeDecoratorFactory(@Nullable final HttpHeadersBuilder exampleHeaders,
                                 final HttpAuthorizer authorizer)
    {
        if(Objects.nonNull(exampleHeaders) && !exampleHeaders.contains(SCOPE_HEADER_NAME))
            exampleHeaders.add(SCOPE_HEADER_NAME, Scope.CLIENT.name());

        this.authorizer = authorizer;
    }

    @Override
    public @NotNull Function<? super HttpService, ? extends HttpService> newDecorator(final @NotNull ScopeService parameter)
    {
        return AuthService.builder()
                .add(authorizer)
                .onSuccess((delegate, ctx, req) -> {
                    final var scope = req.headers().get(SCOPE_HEADER_NAME, Scope.SERVER.name());
                    ctx.setAttr(ScopeContext.ATTR_SCOPE_KEY, Scope.valueOf(scope));

                    return delegate.serve(ctx, req);
                })
                .onFailure((delegate, ctx, req, error) -> {
                    final var scope = req.headers().get(SCOPE_HEADER_NAME, Scope.CLIENT.name());

                    if(scope.contentEquals(Scope.SERVER.name()))
                    {
                        throw new RuntimeException("Unauthorized access to SERVER scope", error);
                    }

                    ctx.setAttr(ScopeContext.ATTR_SCOPE_KEY, Scope.CLIENT);

                    return delegate.serve(ctx, req);
                })
                .newDecorator();
    }
}
