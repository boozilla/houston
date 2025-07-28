package boozilla.houston.container;

import boozilla.houston.asset.sql.Select;
import boozilla.houston.container.interceptor.UpdateInterceptor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;

public final class Houston {
    private static HoustonContainer container;

    static HoustonContainer container()
    {
        return Objects.isNull(container) ? new HoustonContainer() : container;
    }

    static Mono<Void> swap(final HoustonContainer container, final Map<String, UpdateInterceptor<?>> interceptors)
    {
        return Flux.fromIterable(container.updatedSheets())
                .flatMap(sheet -> {
                    final var interceptor = interceptors.get(sheet);

                    if(Objects.isNull(interceptor))
                        return Mono.empty();

                    final var rows = container.query(Select.all().from(sheet));

                    return interceptor.apply(rows);
                })
                .then(Mono.fromRunnable(() -> Houston.container = container));
    }
}
