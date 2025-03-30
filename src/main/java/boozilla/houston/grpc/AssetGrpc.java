package boozilla.houston.grpc;

import boozilla.houston.annotation.ScopeService;
import boozilla.houston.asset.AssetData;
import boozilla.houston.asset.Assets;
import boozilla.houston.context.ScopeContext;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;
import com.linecorp.armeria.common.util.TimeoutMode;
import com.linecorp.armeria.server.ServiceRequestContext;
import houston.grpc.service.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@ScopeService
@AllArgsConstructor
public class AssetGrpc extends ReactorAssetServiceGrpc.AssetServiceImplBase {
    private static final Duration STREAM_EXTEND_TIMEOUT = Duration.ofSeconds(10);

    private final Assets assets;

    @Override
    public Flux<AssetSheet> list(final Empty request)
    {
        final var requestContext = ServiceRequestContext.current();
        final var scope = ScopeContext.get();

        return assets.container()
                .list(scope)
                .doOnNext(any -> requestContext.setRequestTimeout(TimeoutMode.SET_FROM_NOW, STREAM_EXTEND_TIMEOUT));
    }

    @Override
    public Mono<AssetSheets> fetchList(final Empty request)
    {
        return list(request).reduce(AssetSheets.newBuilder(), (builder, sheet) -> {
                    builder.addSheet(sheet);
                    return builder;
                })
                .map(AssetSheets.Builder::build);
    }

    @Override
    public Flux<Any> query(final AssetQueryRequest request)
    {
        final var requestContext = ServiceRequestContext.current();
        final var scope = ScopeContext.get();

        return assets.container()
                .query(scope, request.getQuery(), resultInfo -> {
                    requestContext.addAdditionalResponseHeader("x-houston-query-size", resultInfo.size());
                    requestContext.addAdditionalResponseHeader("x-houston-query-merge-cost", resultInfo.mergeCost());
                    requestContext.addAdditionalResponseHeader("x-houston-query-retrieval-cost", resultInfo.retrievalCost());
                })
                .map(AssetData::any)
                .doOnNext(any -> requestContext.setRequestTimeout(TimeoutMode.SET_FROM_NOW, STREAM_EXTEND_TIMEOUT));
    }

    @Override
    public Mono<AssetQueryResponse> fetchQuery(final AssetQueryRequest request)
    {
        return query(request).reduce(AssetQueryResponse.newBuilder(), (builder, any) -> {
                    builder.addResult(any);
                    return builder;
                })
                .map(AssetQueryResponse.Builder::build);
    }
}
