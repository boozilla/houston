package boozilla.houston.asset;

import boozilla.houston.entity.Data;
import boozilla.houston.repository.DataRepository;
import boozilla.houston.repository.vaults.Vaults;
import com.google.protobuf.InvalidProtocolBufferException;
import houston.vo.asset.Archive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.context.SmartLifecycle;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AssetContainerWatcher implements SmartLifecycle {
    private final Assets assets;
    private final Vaults vaults;
    private final DataRepository repository;

    private final AtomicBoolean hasStarted = new AtomicBoolean(false);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean isRunningSchedule = new AtomicBoolean(false);

    @Override
    public void start()
    {
        if(hasStarted.compareAndSet(false, true))
        {
            try
            {
                log.info("Asset container watcher initial blocking start");
                watch().block();
            }
            catch(Exception e)
            {
                log.error("Initial blocking watch failed", e);
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
        log.info("Asset container watcher stopped");

        running.set(false);
        hasStarted.set(false);
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

        watch().doFinally(signal -> isRunningSchedule.set(false))
                .subscribe();
    }

    public Mono<Void> watch()
    {
        return Mono.fromSupplier(assets::container)
                .flatMap(container -> repository.findByLatest()
                        .collectList()
                        .flatMapMany(latest -> {
                            final var sheetNames = latest.stream()
                                    .map(Data::getSheetName)
                                    .collect(Collectors.toUnmodifiableSet());

                            container.keys().forEach(key -> {
                                if(!sheetNames.contains(key.sheetName()))
                                {
                                    container.remove(key);

                                    log.info("Removed asset (sheetName={}, partitionName={}, scope={})",
                                            key.sheetName(), key.partition().orElse(Strings.EMPTY), key.scope().name());
                                }
                            });

                            return Flux.fromIterable(latest);
                        })
                        .filter(container::different)
                        .flatMap(data -> Mono.defer(() -> downloadArchive(data))
                                .doOnRequest(req -> log.info("Downloading archive (commitId={}, sheetName={}, partitionName={}, scope={})",
                                        data.getCommitId(), data.getSheetName(), data.getPartitionName().orElse(Strings.EMPTY), data.getScope()))
                                .map(archive -> Tuples.of(data, archive)))
                        .collectList()
                        .flatMap(updatedData -> {
                            if(updatedData.isEmpty())
                            {
                                return Mono.empty();
                            }

                            final var updatedContainer = container.copy();
                            return updatedContainer.addAll(updatedData)
                                    .flatMap(AssetContainer::initialize)
                                    .doOnNext(uc -> {
                                        assets.container(uc);
                                        uc.updatedData().forEach(tuple -> {
                                            final var data = tuple.getT1();

                                            log.info("Updated asset (commitId={}, sheetName={}, partitionName={}, scope={})",
                                                    data.getCommitId(), data.getSheetName(), data.getPartitionName().orElse(Strings.EMPTY), data.getScope().name());
                                        });
                                    })
                                    .then();
                        }));
    }

    private Mono<Archive> downloadArchive(Data data)
    {
        return vaults.download(data)
                .flatMap(content -> {
                    try
                    {
                        return Mono.just(Archive.parseFrom(content));
                    }
                    catch(InvalidProtocolBufferException e)
                    {
                        return Mono.error(e);
                    }
                })
                .onErrorResume(error -> {
                    log.error("Archive file not found [path={}]", data.getPath());

                    return Mono.empty();
                });
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
