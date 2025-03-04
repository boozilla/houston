package boozilla.houston.container;

import boozilla.houston.asset.AssetData;
import boozilla.houston.asset.sql.Select;
import boozilla.houston.asset.sql.SqlStatement;
import boozilla.houston.container.interceptor.UpdateInterceptor;
import com.google.protobuf.AbstractMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;

public class Assets {
    private static AssetContainer container;

    static AssetContainer container()
    {
        return Objects.isNull(container) ? new AssetContainer() : container;
    }

    static Mono<Void> swap(final AssetContainer container, final Map<String, UpdateInterceptor<?>> interceptors)
    {
        return Flux.fromIterable(container.updatedSheets())
                .flatMap(sheet -> {
                    final var interceptor = interceptors.get(sheet);

                    if(Objects.isNull(interceptor))
                        return Mono.empty();

                    final var rows = container.query(Select.all().from(sheet));

                    return interceptor.apply(rows);
                })
                .then(Mono.fromRunnable(() -> Assets.container = container));
    }

    public static Flux<AssetData> query(final String sql)
    {
        return container().query(sql);
    }

    public static Flux<AssetData> query(final SqlStatement<?> statement)
    {
        return container().query(statement);
    }

    public static <T extends AbstractMessage> Flux<T> query(final SqlStatement<?> sql, final Class<T> resultClass)
    {
        return query(sql)
                .map(data -> data.message(resultClass));
    }

    public static <T extends AbstractMessage> Mono<T> single(final long code, final Class<T> resultClass)
    {
        return query(Select.all()
                .from(resultClass)
                .where("code = :CODE")
                .parameter("CODE", code)
                .limit(0, 1), resultClass)
                .singleOrEmpty();
    }

    public static <T extends AbstractMessage> Flux<T> many(final Class<T> resultClass)
    {
        return query(Select.all().from(resultClass), resultClass);
    }
}
