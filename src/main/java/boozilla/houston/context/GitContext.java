package boozilla.houston.context;

import reactor.core.publisher.Mono;

import java.util.function.Function;

public interface GitContext<T> {
    <R> Mono<R> api(final Function<T, Mono<R>> function);
}
