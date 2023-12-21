package boozilla.houston.manifest;

import boozilla.houston.repository.ManifestRepository;
import com.github.benmanes.caffeine.cache.AsyncCacheLoader;
import com.google.protobuf.InvalidProtocolBufferException;
import houston.grpc.service.Manifest;
import org.checkerframework.checker.nullness.qual.NonNull;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ManifestLoader implements AsyncCacheLoader<String, Manifest> {
    private final ManifestRepository repository;

    public ManifestLoader(final ManifestRepository repository)
    {
        this.repository = repository;
    }

    @Override
    public @NonNull CompletableFuture<Manifest> asyncLoad(@NonNull final String key, @NonNull final Executor executor)
    {
        return load(key).toFuture();
    }

    public Mono<Manifest> load(final String key)
    {
        return Mono.defer(() -> repository.findById(key))
                .flatMap(manifest -> {
                    try
                    {
                        return Mono.just(Manifest.parseFrom(manifest.getData()));
                    }
                    catch(InvalidProtocolBufferException e)
                    {
                        return Mono.error(e);
                    }
                });
    }
}
