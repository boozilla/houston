package boozilla.houston.container;

import boozilla.houston.container.interceptor.ManifestInterceptor;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Cache;
import com.google.protobuf.AbstractMessageLite;
import houston.grpc.service.Manifest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class ManifestContainer implements SmartLifecycle {
    private final ManifestLoader manifestLoader;
    private final Set<ManifestInterceptor> manifestInterceptors;
    private final AsyncLoadingCache<String, Manifest> cache;

    private final AtomicBoolean hasStarted = new AtomicBoolean(false);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean isRunningSchedule = new AtomicBoolean(false);

    public ManifestContainer(final AsyncLoadingCache<String, Manifest> cache,
                             final ManifestLoader manifestLoader,
                             final Set<ManifestInterceptor> manifestInterceptors)
    {
        this.manifestLoader = manifestLoader;
        this.manifestInterceptors = manifestInterceptors;
        this.cache = cache;
    }

    @Override
    public void start()
    {
        if(hasStarted.compareAndSet(false, true))
        {
            try
            {
                manifestWatcher()
                        .doOnRequest(n -> log.info("Manifest container initial blocking start"))
                        .block();
            }
            catch(Exception e)
            {
                log.error("Initial manifest container watch failed", e);
            }
            finally
            {
                running.set(true);
            }
        }
    }

    @Override
    public void stop()
    {
        running.set(false);
        hasStarted.set(false);

        log.info("Manifest container stopped.");
    }

    @Override
    public boolean isRunning()
    {
        return running.get();
    }

    @Scheduled(fixedDelay = 1000)
    public void scheduleWatch()
    {
        if(!isRunningSchedule.compareAndSet(false, true))
        {
            return;
        }

        manifestWatcher()
                .doFinally(signal -> isRunningSchedule.set(false))
                .subscribe();
    }

    private Mono<Void> manifestWatcher()
    {
        return Flux.fromIterable(cache.asMap().keySet())
                .flatMap(name -> Mono.fromFuture(cache.get(name))
                        .map(AbstractMessageLite::toByteArray)
                        .flatMapMany(oldBytes -> manifestLoader.load(name)
                                .filter(manifest -> {
                                    final var newBytes = manifest.toByteArray();
                                    return !Arrays.equals(oldBytes, newBytes);
                                })
                                .flatMapMany(manifest -> Flux.fromIterable(manifestInterceptors)
                                        .flatMap(interceptor -> interceptor.onUpdate(name, manifest)))
                        ))
                .then();
    }

    public Mono<Manifest> get(final String key)
    {
        final var notExists = !cache.asMap().containsKey(key);

        return Mono.fromFuture(cache.get(key))
                .flatMap(manifest -> {
                    if(notExists)
                    {
                        return Flux.fromIterable(manifestInterceptors)
                                .flatMap(interceptor -> interceptor.onUpdate(key, manifest))
                                .then(Mono.just(manifest));
                    }

                    return Mono.just(manifest);
                });
    }

    public void invalidate()
    {
        Mono.just(cache)
                .map(AsyncLoadingCache::synchronous)
                .doOnNext(Cache::invalidateAll)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    @Override
    public int getPhase()
    {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isAutoStartup()
    {
        return true;
    }

    @Override
    public void stop(Runnable callback)
    {
        stop();
        callback.run();
    }
}
