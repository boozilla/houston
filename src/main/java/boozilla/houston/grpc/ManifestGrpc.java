package boozilla.houston.grpc;

import boozilla.houston.manifest.ManifestContainer;
import houston.grpc.service.Manifest;
import houston.grpc.service.ManifestRetrieveRequest;
import houston.grpc.service.ReactorManifestServiceGrpc;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class ManifestGrpc extends ReactorManifestServiceGrpc.ManifestServiceImplBase {
    private final ManifestContainer container;

    @Override
    public Mono<Manifest> retrieve(final ManifestRetrieveRequest request)
    {
        return container.get(request.getName());
    }
}
