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
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@ScopeService
@AllArgsConstructor
public class AssetGrpc extends ReactorAssetServiceGrpc.AssetServiceImplBase {
    private static final Duration STREAM_EXTEND_TIMEOUT = Duration.ofSeconds(10);
    private static final Pattern SAFE_IDENTIFIER = Pattern.compile("[a-zA-Z0-9_]+");
    private static final int MAX_FILTER_LENGTH = 4096;

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
        return queryData(request)
                .map(AssetData::any);
    }

    public <T> Flux<T> query(final AssetQueryRequest request, final Function<AssetData, Flux<T>> func)
    {
        return queryData(request)
                .flatMapSequential(func);
    }

    public <T> Flux<T> queryMap(final AssetQueryRequest request, final Function<AssetData, T> mapper)
    {
        return queryData(request)
                .map(mapper);
    }

    private Flux<AssetData> queryData(final AssetQueryRequest request)
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

        return response
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

    @Override
    public Flux<Any> search(final AssetSearchRequest request)
    {
        return searchMap(request, AssetData::any);
    }

    public <T> Flux<T> search(final AssetSearchRequest request, final Function<AssetData, Flux<T>> func)
    {
        validateSearchRequest(request);

        final var queryRequest = AssetQueryRequest.newBuilder()
                .setQuery(buildSql(request))
                .setHeadersOnly(request.getHeadersOnly())
                .build();

        return query(queryRequest, func);
    }

    public <T> Flux<T> searchMap(final AssetSearchRequest request, final Function<AssetData, T> mapper)
    {
        validateSearchRequest(request);

        final var queryRequest = AssetQueryRequest.newBuilder()
                .setQuery(buildSql(request))
                .setHeadersOnly(request.getHeadersOnly())
                .build();

        return queryMap(queryRequest, mapper);
    }

    @Override
    public Mono<AssetQueryResponse> fetchSearch(final AssetSearchRequest request)
    {
        return search(request)
                .reduce(AssetQueryResponse.newBuilder(), (builder, any) -> {
                    builder.addResult(any);
                    return builder;
                })
                .map(AssetQueryResponse.Builder::build);
    }

    private void validateSearchRequest(final AssetSearchRequest request)
    {
        if(request.getTable().isEmpty())
        {
            throw new StatusRuntimeException(Status.INVALID_ARGUMENT
                    .withDescription("table is required"));
        }

        if(!SAFE_IDENTIFIER.matcher(request.getTable()).matches())
        {
            throw new StatusRuntimeException(Status.INVALID_ARGUMENT
                    .withDescription("invalid table name: " + request.getTable()));
        }

        for(final var col : request.getColumnList())
        {
            if(!SAFE_IDENTIFIER.matcher(col).matches())
            {
                throw new StatusRuntimeException(Status.INVALID_ARGUMENT
                        .withDescription("invalid column name: " + col));
            }
        }

        for(final var sort : request.getSortList())
        {
            if(!SAFE_IDENTIFIER.matcher(sort.getColumn()).matches())
            {
                throw new StatusRuntimeException(Status.INVALID_ARGUMENT
                        .withDescription("invalid sort column name: " + sort.getColumn()));
            }

            sortOrderToken(sort);
        }

        if(request.getOffset() < 0)
        {
            throw new StatusRuntimeException(Status.INVALID_ARGUMENT
                    .withDescription("offset must not be negative"));
        }

        if(request.getLimit() < 0)
        {
            throw new StatusRuntimeException(Status.INVALID_ARGUMENT
                    .withDescription("limit must not be negative"));
        }

        if(request.getFilter().length() > MAX_FILTER_LENGTH)
        {
            throw new StatusRuntimeException(Status.INVALID_ARGUMENT
                    .withDescription("filter exceeds maximum length of " + MAX_FILTER_LENGTH));
        }
    }

    private String buildSql(final AssetSearchRequest request)
    {
        final var sql = new StringBuilder();

        // SELECT
        if(request.getColumnList().isEmpty())
        {
            sql.append("SELECT *");
        }
        else
        {
            sql.append("SELECT ");
            sql.append(String.join(", ", request.getColumnList()));
        }

        // FROM
        sql.append(" FROM ").append(request.getTable());

        // WHERE
        if(!request.getFilter().isEmpty())
        {
            sql.append(" WHERE ").append(request.getFilter());
        }

        // ORDER BY
        if(!request.getSortList().isEmpty())
        {
            sql.append(" ORDER BY ");
            final var sortJoiner = new StringJoiner(", ");
            for(final var sort : request.getSortList())
            {
                sortJoiner.add(sort.getColumn() + " " + sortOrderToken(sort));
            }
            sql.append(sortJoiner);
        }

        // LIMIT
        if(request.getLimit() > 0)
        {
            if(request.getOffset() > 0)
            {
                sql.append(" LIMIT ").append(request.getOffset()).append(", ").append(request.getLimit());
            }
            else
            {
                sql.append(" LIMIT ").append(request.getLimit());
            }
        }
        else if(request.getOffset() > 0)
        {
            // offset만 있는 경우: LIMIT offset, MAX_VALUE 형태로 처리
            sql.append(" LIMIT ").append(request.getOffset()).append(", ").append(Long.MAX_VALUE);
        }

        return sql.toString();
    }

    private static String sortOrderToken(final AssetSearchRequest.SortCriteria sort)
    {
        return switch(sort.getOrder())
        {
            case ASC -> "ASC";
            case DESC -> "DESC";
            case UNRECOGNIZED -> throw new StatusRuntimeException(Status.INVALID_ARGUMENT
                    .withDescription("invalid sort order for column '" + sort.getColumn()
                            + "' (orderValue=" + sort.getOrderValue() + ")"));
        };
    }
}
