package boozilla.houston.manifest;

import boozilla.houston.repository.ManifestRepository;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import houston.grpc.service.Manifest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class ManifestContainer implements SmartLifecycle {
    private final ManifestLoader loader;
    private final AsyncLoadingCache<String, Manifest> cache;

    private final AtomicBoolean hasStarted = new AtomicBoolean(false);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean isRunningSchedule = new AtomicBoolean(false);

    public ManifestContainer(final ManifestRepository repository)
    {
        this.loader = new ManifestLoader(repository);
        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofHours(1))
                .buildAsync(this.loader);
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
                .flatMap(this.loader::load)
                .then()
                .onErrorResume(error -> {
                    log.error("Error while watching manifest repository", error);
                    return Mono.empty();
                });
    }

    public Mono<Manifest> get(final String key)
    {
        return Mono.fromFuture(cache.get(key));
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
