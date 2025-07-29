package boozilla.houston.container;

import boozilla.houston.HoustonChannel;
import houston.grpc.service.Manifest;
import houston.grpc.service.ManifestRetrieveRequest;
import houston.grpc.service.ReactorManifestServiceGrpc;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@ConditionalOnBean(HoustonChannel.class)
@AllArgsConstructor
public class ManifestHoustonLoader implements ManifestLoader {
    private final HoustonChannel channel;

    @Override
    public Mono<Manifest> load(final String key)
    {
        final var request = ManifestRetrieveRequest.newBuilder()
                .setName(key)
                .build();

        return ReactorManifestServiceGrpc.newReactorStub(channel)
                .retrieve(request);
    }
}
