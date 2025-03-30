package boozilla.houston.asset.constraints;

import boozilla.houston.asset.AssetData;
import boozilla.houston.asset.AssetLink;
import boozilla.houston.asset.QueryResultInfo;
import boozilla.houston.asset.Scope;
import boozilla.houston.asset.sql.SqlStatement;
import com.google.protobuf.JavaType;
import reactor.core.publisher.Flux;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public interface AssetAccessor {
    Set<Key> updatedKey();

    Set<Key> updatedMergeKey();

    Flux<AssetData> query(final AssetLink link, final SqlStatement<?> sql);

    Flux<AssetData> query(final AssetLink link, final SqlStatement<?> sql, final Consumer<QueryResultInfo> resultInfoConsumer);

    Flux<AssetData> query(final Scope scope, final SqlStatement<?> sql);

    Flux<AssetData> query(final Scope scope, final SqlStatement<?> sql, final Consumer<QueryResultInfo> resultInfoConsumer);

    Flux<AssetData> query(final AssetLink link, final String sql);

    Flux<AssetData> query(final AssetLink link, final String sql, final Consumer<QueryResultInfo> resultInfoConsumer);

    Flux<AssetData> query(final Scope scope, final String sql);

    Flux<AssetData> query(final Scope scope, final String sql, final Consumer<QueryResultInfo> resultInfoConsumer);

    Flux<AssetLink> links(final String sheetName);

    JavaType columnType(final String sheetName, final String columnName);

    record Key(
            String sheetName,
            Optional<String> partition,
            Scope scope
    ) {
        public Key(final String sheetName, final Scope scope)
        {
            this(sheetName, Optional.empty(), scope);
        }

        public Key toMergeKey()
        {
            return new Key(sheetName, scope);
        }

        public int hashCode()
        {
            return Objects.hash(sheetName.toUpperCase(), partition, scope);
        }
    }
}
