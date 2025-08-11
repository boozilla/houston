package boozilla.houston.container;

import boozilla.houston.asset.AssetData;
import boozilla.houston.asset.sql.Select;
import boozilla.houston.asset.sql.SqlStatement;
import com.google.protobuf.AbstractMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

public class Assets {
    public Flux<AssetData> query(final String sql)
    {
        return Houston.container().query(sql);
    }

    public Flux<AssetData> query(final SqlStatement<?> statement)
    {
        return Houston.container().query(statement);
    }

    public <T extends AbstractMessage> Flux<T> query(final SqlStatement<?> sql, final Class<T> resultClass)
    {
        return query(sql)
                .map(data -> data.message(resultClass));
    }

    public <T extends AbstractMessage> Mono<T> single(final long code, final Class<T> resultClass)
    {
        return query(Select.all()
                .from(resultClass)
                .where("code = :CODE")
                .parameter("CODE", code)
                .limit(0, 1), resultClass)
                .singleOrEmpty();
    }

    public <T extends AbstractMessage> Flux<T> many(final Class<T> resultClass)
    {
        return query(Select.all().from(resultClass), resultClass);
    }

    public Mono<Boolean> exists(final long code, final Class<? extends AbstractMessage> resultClass)
    {
        return single(code, resultClass)
                .map(Objects::nonNull)
                .defaultIfEmpty(false);
    }

    public String version(final Class<? extends AbstractMessage> targetClass)
    {
        return Houston.container().commitId(targetClass.getSimpleName());
    }
}
