package boozilla.houston.unframed;

import boozilla.houston.HoustonHeaders;
import boozilla.houston.annotation.ScopeService;
import boozilla.houston.asset.AssetContainers;
import boozilla.houston.asset.AssetData;
import boozilla.houston.asset.Scope;
import boozilla.houston.asset.codec.ProtobufRowCodec;
import boozilla.houston.context.ScopeContext;
import boozilla.houston.grpc.AssetGrpc;
import boozilla.houston.repository.DataRepository;
import boozilla.houston.repository.vaults.Vaults;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.linecorp.armeria.common.*;
import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.Param;
import com.linecorp.armeria.server.annotation.Post;
import houston.grpc.service.AssetListRequest;
import houston.grpc.service.AssetQueryRequest;
import houston.vo.asset.Archive;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@RestController
@AllArgsConstructor
public class AssetUnframedService implements UnframedService {
    private static final Pattern SAFE_IDENTIFIER = Pattern.compile("[a-zA-Z0-9_\\-]+");

    private final AssetGrpc assetGrpc;
    private final AssetContainers assetContainers;
    private final DataRepository dataRepository;
    private final Vaults vaults;

    @ScopeService
    @Post("/asset/query")
    public Mono<String> query(final AssetQueryRequest request)
    {
        return assetGrpc.query(request, data -> Flux.just(data.toJsonString()))
                .collect(Collectors.joining(",", "[", "]"));
    }

    @ScopeService
    @Get("/asset/{tableName}")
    public Mono<HttpResponse> redirect(@Param("tableName") final String tableName)
    {
        if(!SAFE_IDENTIFIER.matcher(tableName).matches())
            return Mono.just(HttpResponse.of(HttpStatus.BAD_REQUEST));

        final var request = AssetListRequest.newBuilder()
                .addInclude(tableName)
                .build();

        return assetGrpc.fetchList(request)
                .flatMap(sheets -> {
                    final var sheetList = sheets.getSheetList();

                    if(sheetList.isEmpty())
                        return Mono.just(HttpResponse.of(HttpStatus.NOT_FOUND));

                    final var commitId = sheetList.getFirst().getCommitId();
                    return Mono.just(HttpResponse.of(
                            ResponseHeaders.builder(HttpStatus.FOUND)
                                    .add(HttpHeaderNames.LOCATION, "/asset/" + tableName + "/" + commitId)
                                    .add(HttpHeaderNames.CACHE_CONTROL, "no-store")
                                    .build()));
                });
    }

    @ScopeService
    @Get("/asset/{tableName}/{commitId}")
    public Mono<HttpResponse> getData(@Param("tableName") final String tableName,
                                      @Param("commitId") final String commitId)
    {
        if(!SAFE_IDENTIFIER.matcher(tableName).matches() || !SAFE_IDENTIFIER.matcher(commitId).matches())
            return Mono.just(HttpResponse.of(HttpStatus.BAD_REQUEST));

        final var scope = ScopeContext.get();
        final var container = assetContainers.container();

        return container.list(scope, Set.of(tableName))
                .next()
                .flatMap(sheet -> {
                    if(sheet.getCommitId().equals(commitId))
                    {
                        // Fast path: 인메모리 쿼리
                        return queryFromContainer(tableName);
                    }

                    // Slow path: Vault에서 로드
                    return queryFromVault(commitId, tableName, scope);
                })
                .switchIfEmpty(Mono.defer(() -> queryFromVault(commitId, tableName, scope)))
                .map(json -> HttpResponse.of(
                        ResponseHeaders.builder(HttpStatus.OK)
                                .contentType(MediaType.JSON_UTF_8)
                                .add(HttpHeaderNames.CACHE_CONTROL, "public, max-age=31536000, immutable")
                                .add(HttpHeaderNames.VARY, HoustonHeaders.SCOPE)
                                .build(),
                        HttpData.ofUtf8(json)))
                .switchIfEmpty(Mono.just(HttpResponse.of(HttpStatus.NOT_FOUND)));
    }

    private Mono<String> queryFromContainer(final String tableName)
    {
        final var request = AssetQueryRequest.newBuilder()
                .setQuery("SELECT * FROM " + tableName)
                .build();

        return assetGrpc.query(request, data -> Flux.just(data.toJsonString()))
                .collect(Collectors.joining(",", "[", "]"))
                .onErrorResume(StatusRuntimeException.class, error -> {
                    if(error.getStatus().getCode() == Status.Code.NOT_FOUND)
                        return Mono.empty();
                    return Mono.error(error);
                });
    }

    private Mono<String> queryFromVault(final String commitId, final String tableName,
                                        final Scope scope)
    {
        return dataRepository.findByCommitIdAndScopeAndSheetName(commitId, scope.name(), tableName)
                .flatMap(data -> vaults.download(data)
                        .flatMap(bytes -> {
                            try
                            {
                                return Mono.just(Archive.parseFrom(bytes));
                            }
                            catch(Exception e)
                            {
                                return Mono.error(e);
                            }
                        }))
                .flatMap(archive -> {
                    try
                    {
                        final var descriptorProto = DescriptorProtos.FileDescriptorProto
                                .parseFrom(archive.getDescriptorBytes());
                        final var fileDescriptor = Descriptors.FileDescriptor
                                .buildFrom(descriptorProto, new Descriptors.FileDescriptor[0]);
                        final var codec = new ProtobufRowCodec(tableName, fileDescriptor);
                        final var messages = codec.deserialize(archive.getDataBytes().toByteArray());

                        return Flux.fromIterable(messages)
                                .map(msg -> new AssetData(msg, codec.getFieldDescriptor())
                                        .toJsonString());
                    }
                    catch(Exception e)
                    {
                        return Flux.error(e);
                    }
                })
                .collect(Collectors.joining(",", "[", "]"))
                .filter(json -> !json.equals("[]"))
                .onErrorResume(e -> {
                    log.error("Failed to load asset from vault (commitId={}, table={})", commitId, tableName, e);
                    return Mono.empty();
                });
    }
}
