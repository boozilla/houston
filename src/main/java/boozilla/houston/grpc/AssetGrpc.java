package boozilla.houston.grpc;

import boozilla.houston.HoustonHeaders;
import boozilla.houston.annotation.ScopeService;
import boozilla.houston.asset.AssetContainers;
import boozilla.houston.asset.AssetData;
import boozilla.houston.context.ScopeContext;
import boozilla.houston.exception.AssetQueryException;
import com.google.protobuf.Any;
import com.linecorp.armeria.common.util.TimeoutMode;
import com.linecorp.armeria.server.ServiceRequestContext;
import houston.grpc.service.*;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@ScopeService
@AllArgsConstructor
public class AssetGrpc extends ReactorAssetServiceGrpc.AssetServiceImplBase {
    private static final Duration STREAM_EXTEND_TIMEOUT = Duration.ofSeconds(10);

    private final AssetContainers assets;

    @Override
    public Flux<AssetSheet> list(final AssetListRequest request)
    {
        final var requestContext = ServiceRequestContext.current();
        final var scope = ScopeContext.get();
        final var include = request.getIncludeList().stream()
                .collect(Collectors.toUnmodifiableSet());

        return assets.container()
                .list(scope, include)
                .doOnNext(_ -> requestContext.setRequestTimeout(TimeoutMode.SET_FROM_NOW, STREAM_EXTEND_TIMEOUT));
    }

    @Override
    public Mono<AssetSheets> fetchList(final AssetListRequest request)
    {
        return list(request)
                .reduce(AssetSheets.newBuilder(), (builder, sheet) -> {
                    builder.addSheet(sheet);
                    return builder;
                })
                .map(AssetSheets.Builder::build);
    }

    @Override
    public Flux<Any> query(final AssetQueryRequest request)
    {
        return query(request, data -> Flux.just(data.any()));
    }

    public <T> Flux<T> query(final AssetQueryRequest request, final Function<AssetData, Flux<T>> func)
    {
        final var requestContext = ServiceRequestContext.current();
        final var scope = ScopeContext.get();

        final var response = assets.container()
                .query(scope, request.getQuery(), resultInfo -> {
                    requestContext.addAdditionalResponseHeader(HoustonHeaders.COMMIT_ID, resultInfo.commitId());
                    requestContext.addAdditionalResponseHeader(HoustonHeaders.QUERY_SIZE, resultInfo.size());
                    requestContext.addAdditionalResponseHeader(HoustonHeaders.QUERY_MERGE_COST, resultInfo.mergeCost());
                    requestContext.addAdditionalResponseHeader(HoustonHeaders.QUERY_RETRIEVAL_COST, resultInfo.retrievalCost());
                });

        if(request.getHeadersOnly())
        {
            return response.thenMany(Flux.empty());
        }

        return response.flatMapSequential(func)
                .doOnNext(_ -> requestContext.setRequestTimeout(TimeoutMode.SET_FROM_NOW, STREAM_EXTEND_TIMEOUT))
                .onErrorMap(AssetQueryException.class, error -> new StatusRuntimeException(Status.NOT_FOUND
                        .withDescription(error.getMessage())
                        .withCause(error)));
    }

    @Override
    public Mono<AssetQueryResponse> fetchQuery(final AssetQueryRequest request)
    {
        return query(request)
                .reduce(AssetQueryResponse.newBuilder(), (builder, any) -> {
                    builder.addResult(any);
                    return builder;
                })
                .map(AssetQueryResponse.Builder::build);
    }
}
