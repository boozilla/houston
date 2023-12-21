package boozilla.houston.asset;

import boozilla.houston.entity.Data;
import boozilla.houston.repository.DataRepository;
import boozilla.houston.repository.Vaults;
import houston.vo.asset.Archive;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuples;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class AssetContainerWatcher {
    private final Assets assets;
    private final Vaults vaults;
    private final DataRepository repository;

    @PostConstruct
    private void watch()
    {
        final var watcher = Mono.fromSupplier(assets::container)
                // 최신 애셋 데이터 정보 조회
                .flatMap(container -> repository.findByLatest()
                        .collectList()
                        .flatMapMany(latest -> {
                            final var sheetNames = latest.stream().map(Data::getSheetName)
                                    .collect(Collectors.toUnmodifiableSet());

                            container.keys().forEach(key -> {
                                if(!sheetNames.contains(key.sheetName()))
                                {
                                    container.remove(key);

                                    log.info("The asset data has been removed (sheetName = {}, partitionName = {}, scope = {})",
                                            key.sheetName(), key.partition().orElse(Strings.EMPTY), key.scope().name());
                                }
                            });

                            return Flux.fromIterable(latest);
                        })
                        // 현재 컨테이너와 비교
                        .filter(container::different)
                        .flatMap(data -> Mono.defer(() -> downloadArchive(data))
                                .doOnRequest(request -> log.info("Downloading archive (commitId = {}, sheetName = {}, partitionName = {}, scope = {})",
                                        data.getCommitId(), data.getSheetName(), data.getPartitionName(), data.getScope()))
                                .map(archive -> Tuples.of(data, archive)))
                        .collectList()
                        // 변경된 애셋 데이터 정보를 새 컨테이너에 반영
                        .flatMap(updatedData -> {
                            final var updatedContainer = container.copy();
                            return updatedContainer.addAll(updatedData);
                        })
                        .doOnNext(AssetContainer::initialize)
                        // 현재 컨테이너를 새 컨테이너로 교체
                        .doOnNext(updatedContainer -> {
                            assets.container(updatedContainer);

                            updatedContainer.updatedData()
                                    .forEach(tuple -> {
                                        final var data = tuple.getT1();

                                        log.info("The asset data has been updated (commitId = {}, sheetName = {}, partitionName = {}, scope = {})",
                                                data.getCommitId(), data.getSheetName(), data.getPartitionName().orElse(Strings.EMPTY), data.getScope().name());
                                    });
                        }));

        watcher.doFinally(signal -> watcher.repeat()
                        .delayUntil(currentContainer -> Mono.delay(Duration.ofSeconds(1)))
                        .subscribeOn(Schedulers.boundedElastic())
                        .subscribe(
                                success -> {
                                },
                                error -> log.error("Error watching asset data", error)
                        ))
                .block();
    }

    private Mono<Archive> downloadArchive(final Data data)
    {
        final var path = data.getSheetName() + "/" + data.getSha256();

        return vaults.download(path)
                .onErrorResume(error -> {
                    log.error("Archive file not found [path=%s]".formatted(path));
                    return Mono.empty();
                });
    }
}
