package boozilla.houston.container;

import com.github.benmanes.caffeine.cache.AsyncCacheLoader;
import houston.grpc.service.Manifest;
import org.checkerframework.checker.nullness.qual.NonNull;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface ManifestLoader extends AsyncCacheLoader<String, Manifest> {
    @Override
    default @NonNull CompletableFuture<Manifest> asyncLoad(@NonNull final String key, @NonNull final Executor executor)
    {
        return load(key).toFuture();
    }

    Mono<Manifest> load(final String key);
}
