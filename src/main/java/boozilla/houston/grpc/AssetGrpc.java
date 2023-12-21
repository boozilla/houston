package boozilla.houston.grpc;

import boozilla.houston.annotation.ScopeService;
import boozilla.houston.asset.AssetData;
import boozilla.houston.asset.Assets;
import boozilla.houston.context.ScopeContext;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;
import houston.grpc.service.AssetQueryRequest;
import houston.grpc.service.AssetSheet;
import houston.grpc.service.ReactorAssetServiceGrpc;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

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
    public Flux<Any> query(final AssetQueryRequest request)
    {
        final var scope = ScopeContext.get();

        return assets.container()
                .query(scope, request.getQuery())
                .map(AssetData::any);
    }
}
