package boozilla.houston.rest;

import boozilla.houston.annotation.ScopeService;
import boozilla.houston.asset.AssetData;
import boozilla.houston.asset.Assets;
import boozilla.houston.context.ScopeContext;
import com.linecorp.armeria.server.annotation.Post;
import houston.grpc.service.AssetQueryRequest;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class AssetUnframedService implements RestService {
    private final Assets assets;

    @ScopeService
    @Post("/asset/query")
    public Mono<String> query(final AssetQueryRequest request)
    {
        final var scope = ScopeContext.get();

        return assets.container()
                .query(scope, request.getQuery())
                .map(AssetData::toJsonString)
                .collect(Collectors.joining(",", "[", "]"));
    }
}
