package boozilla.houston.container.interceptor;

import boozilla.houston.asset.AssetData;
import com.google.protobuf.GeneratedMessageV3;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UpdateInterceptor<Sheet extends GeneratedMessageV3> {
    Class<Sheet> targetTable();

    default Mono<Void> apply(final Flux<AssetData> rows)
    {
        return rows.map(data -> data.message(this.targetTable()))
                .transform(this::update)
                .then();
    }

    Mono<Void> update(final Flux<Sheet> rows);
}
