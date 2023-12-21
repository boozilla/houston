package boozilla.houston.container;

import boozilla.houston.HoustonChannel;
import boozilla.houston.container.interceptor.UpdateInterceptor;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;
import houston.grpc.service.AssetQueryRequest;
import houston.grpc.service.AssetSheet;
import houston.grpc.service.ReactorAssetServiceGrpc;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuples;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class AssetContainerWatcher implements AutoCloseable {
    private final HoustonChannel channel;
    private final Map<String, UpdateInterceptor<?>> interceptors;

    public AssetContainerWatcher(final HoustonChannel channel, final Set<UpdateInterceptor<?>> interceptors)
    {
        this.channel = channel;
        this.interceptors = interceptors.stream()
                .collect(Collectors.toUnmodifiableMap(
                        interceptor -> interceptor.targetTable().getSimpleName(),
                        interceptor -> interceptor
                ));
    }

    @PostConstruct
    public void watch()
    {
        final var watcher = Mono.fromSupplier(Assets::container)
                .flatMap(current -> list(current)
                        .flatMap(latest -> diff(current, latest)
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
                .flatMap(container -> Assets.swap(container, interceptors)
                        .thenReturn(container))
                .onErrorResume(error -> {
                    log.error("Error while watching asset container", error);
                    return Mono.empty();
                });

        watcher.doFinally(signal -> watcher.repeat()
                        .delayUntil(container -> Mono.delay(Duration.ofSeconds(1)))
                        .subscribeOn(Schedulers.boundedElastic())
                        .subscribe())
                .block();
    }

    @Override
    public void close()
    {
        if(Objects.nonNull(channel))
        {
            channel.close();
        }
    }

    private Mono<List<AssetSheet>> list(final AssetContainer container)
    {
        final var stub = ReactorAssetServiceGrpc.newReactorStub(channel);
        return stub.list(Empty.getDefaultInstance())
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

    private Mono<List<AssetSheet>> diff(final AssetContainer container, final List<AssetSheet> latest)
    {
        final var currentSheets = container.sheets();

        return Mono.just(latest)
                .filter(list -> list.stream().noneMatch(e -> currentSheets.stream()
                        .anyMatch(sheet -> sheet.getName().contentEquals(e.getName()) &&
                                sheet.getCommitId().contentEquals(e.getCommitId()))));
    }

    private Flux<Any> download(final AssetSheet assetSheet)
    {
        final var stub = ReactorAssetServiceGrpc.newReactorStub(channel);
        return stub.query(AssetQueryRequest.newBuilder()
                .setQuery("SELECT * FROM " + assetSheet.getName())
                .build());
    }
}
