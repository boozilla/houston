package boozilla.houston.decorator.factory;

import boozilla.houston.annotation.SecuredService;
import boozilla.houston.decorator.GrpcAuthDecorator;
import com.linecorp.armeria.server.HttpService;
import com.linecorp.armeria.server.annotation.DecoratorFactoryFunction;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public final class SecureDecoratorFactory implements DecoratorFactoryFunction<@NotNull SecuredService> {
    private final GrpcAuthDecorator grpcAuthDecorator;

    public SecureDecoratorFactory(final GrpcAuthDecorator grpcAuthDecorator)
    {
        this.grpcAuthDecorator = grpcAuthDecorator;
    }

    @Override
    public @NotNull Function<? super HttpService, ? extends HttpService> newDecorator(@NotNull final SecuredService parameter)
    {
        return grpcAuthDecorator;
    }
}