package boozilla.houston.unframed;

import boozilla.houston.annotation.ScopeService;
import boozilla.houston.grpc.AssetGrpc;
import com.linecorp.armeria.server.annotation.Post;
import houston.grpc.service.AssetQueryRequest;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class AssetUnframedService implements UnframedService {
    private final AssetGrpc assetGrpc;

    @ScopeService
    @Post("/asset/query")
    public Mono<String> query(final AssetQueryRequest request)
    {
        return assetGrpc.query(request, data -> Flux.just(data.toJsonString()))
                .collect(Collectors.joining(",", "[", "]"));
    }
}
