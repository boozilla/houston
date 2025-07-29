package boozilla.houston.container;

import boozilla.houston.HoustonChannel;
import boozilla.houston.container.interceptor.UpdateInterceptor;
import com.google.protobuf.Any;
import houston.grpc.service.AssetListRequest;
import houston.grpc.service.AssetQueryRequest;
import houston.grpc.service.AssetSheet;
import houston.grpc.service.ReactorAssetServiceGrpc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.SmartLifecycle;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
@Component
@ConditionalOnBean(HoustonChannel.class)
public class HoustonWatcher implements SmartLifecycle {
    private final HoustonChannel channel;
    private final Map<String, UpdateInterceptor<?>> updateInterceptors;

    private final AtomicBoolean hasStarted = new AtomicBoolean(false);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean isRunningSchedule = new AtomicBoolean(false);

    public HoustonWatcher(final HoustonChannel channel,
                          final Set<UpdateInterceptor<?>> updateInterceptors)
    {
        this.channel = channel;
        this.updateInterceptors = updateInterceptors.stream()
                .collect(Collectors.toUnmodifiableMap(
                        interceptor -> interceptor.targetTable().getSimpleName(),
                        interceptor -> interceptor
                ));
    }

    @Override
    public void start()
    {
        if(hasStarted.compareAndSet(false, true))
        {
            try
            {
                containerWatcher()
                        .doOnRequest(n -> log.info("Asset container watcher initial blocking start"))
                        .block();
            }
            catch(Exception e)
            {
                log.error("Initial asset container watch failed", e);
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
        if(channel != null)
        {
            channel.close();
        }

        running.set(false);
        hasStarted.set(false);

        log.info("Asset container watcher stopped.");
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

        containerWatcher()
                .doFinally(signal -> isRunningSchedule.set(false))
                .subscribe();
    }

    private Mono<HoustonContainer> containerWatcher()
    {
        return Mono.fromSupplier(Houston::container)
                .flatMap(current -> list(current)
                        .flatMap(latest -> assetDiff(current, latest)
                                .filter(diff -> !diff.isEmpty())
                                .flatMapMany(diff -> Flux.fromIterable(diff)
                                        .flatMap(sheet -> download(sheet)
                                                .collectList()
                                                .map(data -> Tuples.of(sheet, data))))
                                .reduce(current.copy(), (container, tuple) -> {
                                    final var sheet = tuple.getT1();
                                    final var data = tuple.getT2();

                                    container.add(sheet, data);

                                    return container;
                                })))
                .flatMap(container -> Houston.swap(container, updateInterceptors)
                        .thenReturn(container))
                .onErrorResume(error -> {
                    log.error("Error while watching asset container", error);

                    return Mono.empty();
                });
    }

    private Mono<List<AssetSheet>> list(final HoustonContainer container)
    {
        final var stub = ReactorAssetServiceGrpc.newReactorStub(channel);
        return stub.list(AssetListRequest.getDefaultInstance())
                .collectList()
                .doOnNext(latest -> {
                    final var sheetNames = latest.stream().map(AssetSheet::getName)
                            .collect(Collectors.toUnmodifiableSet());

                    container.sheets().forEach(sheet -> {
                        if(!sheetNames.contains(sheet.getName()))
                        {
                            container.remove(sheet);
                        }
                    });
                });
    }

    private Mono<List<AssetSheet>> assetDiff(final HoustonContainer container, final List<AssetSheet> latest)
    {
        final var currentSheets = container.sheets();

        return Mono.just(latest)
                .filter(list -> list.stream().noneMatch(e -> currentSheets.stream()
                        .anyMatch(sheet -> sheet.getName().equals(e.getName()) &&
                                sheet.getCommitId().equals(e.getCommitId()))));
    }

    private Flux<Any> download(final AssetSheet assetSheet)
    {
        final var stub = ReactorAssetServiceGrpc.newReactorStub(channel);
        return stub.query(AssetQueryRequest.newBuilder()
                .setQuery("SELECT * FROM " + assetSheet.getName())
                .build());
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
