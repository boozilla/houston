package boozilla.houston.decorator.factory;

import boozilla.houston.annotation.SecuredService;
import boozilla.houston.decorator.AdminAuthDecorator;
import boozilla.houston.decorator.auth.HttpAuthorizer;
import com.linecorp.armeria.server.HttpService;
import com.linecorp.armeria.server.annotation.DecoratorFactoryFunction;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public final class SecureDecoratorFactory implements DecoratorFactoryFunction<@NotNull SecuredService> {
    private final List<HttpAuthorizer> authorizers;

    public SecureDecoratorFactory(final List<HttpAuthorizer> authorizers)
    {
        this.authorizers = authorizers;
    }

    @Override
    public @NotNull Function<? super HttpService, ? extends HttpService> newDecorator(@NotNull final SecuredService parameter)
    {
        final var allowAuthorizers = authorizers.stream()
                .filter(authorizer -> Arrays.stream(parameter.value())
                        .anyMatch(a -> a.isAssignableFrom(authorizer.getClass())))
                .toList();

        return new AdminAuthDecorator(allowAuthorizers);
    }
}
