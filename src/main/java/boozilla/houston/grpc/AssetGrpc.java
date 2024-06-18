package boozilla.houston.grpc;

import boozilla.houston.annotation.ScopeService;
import boozilla.houston.asset.AssetData;
import boozilla.houston.asset.Assets;
import boozilla.houston.context.ScopeContext;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;
import houston.grpc.service.*;
import lombok.AllArgsConstructor;
import org.curioswitch.common.protobuf.json.SerializeSupport;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class AssetGrpc extends ReactorAssetServiceGrpc.AssetServiceImplBase {
    private final Assets assets;

    @Override
    @ScopeService
    public Flux<AssetSheet> list(final Empty request)
    {
        final var scope = ScopeContext.get();

        return assets.container()
                .list(scope);
    }

    @Override
    @ScopeService
    public Mono<AssetSheets> fetchList(final Empty request)
    {
        return list(request).reduce(AssetSheets.newBuilder(), (builder, sheet) -> {
                    builder.addSheet(sheet);
                    return builder;
                })
                .map(AssetSheets.Builder::build);
    }

    @Override
    @ScopeService
    public Flux<Any> query(final AssetQueryRequest request)
    {
        final var scope = ScopeContext.get();

        return assets.container()
                .query(scope, request.getQuery())
                .map(AssetData::any);
    }

    @Override
    @ScopeService
    public Mono<AssetQueryResponse> fetchQuery(final AssetQueryRequest request)
    {
        // TODO Any 의 url 이 가르키는 Json marshaller 를 동적으로 등록해야 함
        return query(request).reduce(AssetQueryResponse.newBuilder(), (builder, any) -> {
                    builder.addResult(any);
                    return builder;
                })
                .map(AssetQueryResponse.Builder::build);
    }
}
