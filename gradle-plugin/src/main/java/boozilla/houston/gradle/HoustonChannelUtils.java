package boozilla.houston.gradle;

import boozilla.houston.HoustonChannel;
import boozilla.houston.asset.Scope;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public class HoustonChannelUtils {
    /**
     * Executes a given channel function on a Houston channel obtained from the provided environment.
     *
     * @param environment     The Houston environment.
     * @param channelFunction The channel function to execute on the Houston channel.
     * @param <T>             The type of the resulting Mono.
     * @return A Mono representing the result of the channel function execution.
     */
    public static <T> Mono<T> mono(final HoustonEnvironment environment, final Function<HoustonChannel, Mono<T>> channelFunction)
    {
        final var url = environment.getUrl().get();
        final var token = environment.getToken().get();
        final var scope = Scope.valueOf(environment.getScope().get());
        final var tls = environment.getTls().getOrElse(false);

        return Mono.using(() -> new boozilla.houston.HoustonChannel(url, token, scope, tls),
                channelFunction,
                boozilla.houston.HoustonChannel::close);
    }

    /**
     * Create a Flux using a HoustonEnvironment and a channel function.
     *
     * @param <T>             The type of elements in the Flux.
     * @param environment     The HoustonEnvironment object.
     * @param channelFunction A function that takes a HoustonChannel and returns a Flux of type T.
     * @return The Flux object created using the HoustonEnvironment and channel function.
     */
    public static <T> Flux<T> flux(final HoustonEnvironment environment, final Function<HoustonChannel, Flux<T>> channelFunction)
    {
        final var url = environment.getUrl().get();
        final var token = environment.getToken().get();
        final var scope = Scope.valueOf(environment.getScope().get());
        final var tls = environment.getTls().getOrElse(false);

        return Flux.using(() -> new HoustonChannel(url, token, scope, tls),
                channelFunction,
                HoustonChannel::close);
    }
}
