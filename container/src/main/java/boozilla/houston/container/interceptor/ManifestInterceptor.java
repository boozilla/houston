package boozilla.houston.container.interceptor;

import houston.grpc.service.Manifest;
import reactor.core.publisher.Mono;

public interface ManifestInterceptor {
    Mono<Void> onUpdate(final String name, final Manifest manifest);
}
