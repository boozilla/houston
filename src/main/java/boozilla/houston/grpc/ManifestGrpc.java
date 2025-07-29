package boozilla.houston.grpc;

import boozilla.houston.common.AdminAddress;
import boozilla.houston.container.ManifestContainer;
import com.linecorp.armeria.server.ServiceRequestContext;
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
    private final AdminAddress adminAddress;

    @Override
    public Mono<Manifest> retrieve(final ManifestRetrieveRequest request)
    {
        final var clientAddress = ServiceRequestContext.current()
                .clientAddress();
        final var isAdmin = adminAddress.is(clientAddress);

        return container.get(request.getName())
                .map(manifest -> {
                    if(isAdmin)
                    {
                        return Manifest.newBuilder(manifest)
                                .clearMaintenance()
                                .build();
                    }

                    return manifest;
                })
                .defaultIfEmpty(Manifest.getDefaultInstance());
    }
}
