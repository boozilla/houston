package boozilla.houston.manifest;

import boozilla.houston.repository.ManifestRepository;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import houston.grpc.service.Manifest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import java.time.Duration;

@Slf4j
@Component
public class ManifestContainer {
    private final ManifestLoader loader;
    private final AsyncLoadingCache<String, Manifest> cache;

    public ManifestContainer(final ManifestRepository repository)
    {
        this.loader = new ManifestLoader(repository);
        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofHours(1))
                .buildAsync(this.loader);
    }

    @PostConstruct
    private void watch()
    {
        final var watcher = Flux.fromIterable(cache.asMap().keySet())
                .flatMap(this.loader::load);

        watcher.doFinally(signal -> watcher.repeat()
                        .delayUntil(container -> Mono.delay(Duration.ofSeconds(1)))
                        .subscribeOn(Schedulers.boundedElastic())
                        .subscribe(
                                success -> {
                                },
                                error -> log.error("Error while watching manifest repository", error)
                        ))
                .blockLast();
    }

    public Mono<Manifest> get(final String key)
    {
        return Mono.fromFuture(cache.get(key));
    }
}
