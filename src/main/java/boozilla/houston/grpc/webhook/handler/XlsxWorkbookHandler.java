package boozilla.houston.grpc.webhook.handler;

import boozilla.houston.Application;
import boozilla.houston.asset.*;
import boozilla.houston.asset.codec.AssetLinkCodec;
import boozilla.houston.asset.codec.ProtobufRowCodec;
import boozilla.houston.asset.codec.ProtobufSchemaSerializer;
import boozilla.houston.entity.Data;
import boozilla.houston.grpc.webhook.GitBehavior;
import boozilla.houston.repository.DataRepository;
import com.google.protobuf.ByteString;
import houston.vo.asset.Archive;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class XlsxWorkbookHandler extends GitFileHandler {
    private final AssetContainer container;
    private final List<Throwable> sheetExceptions;

    public XlsxWorkbookHandler(final String projectId, final String issueId, final String commitId, final String packageName,
                               final GitBehavior<?> behavior)
    {
        super(projectId, issueId, commitId, packageName, behavior);

        this.container = Application.assets().container().copy();
        this.sheetExceptions = new ArrayList<>();
    }

    @Override
    public Mono<XlsxWorkbookHandler> add(final String path, final byte[] bytes)
    {
        return Mono.usingWhen(AssetInputStream.open(path, bytes),
                in -> AssetReader.of(in)
                        // Scope x Sheet 의 Data 엔티티 생성
                        .flatMapMany(reader -> Flux.fromArray(Scope.values())
                                .flatMap(scope -> reader.sheets()
                                        .flatMap(sheet -> toData(commitId, packageName, sheet, scope, sheetExceptions))))
                        // Sandbox 컨테이너 구성
                        .doOnNext(tuple -> container.add(tuple.getT1(), tuple.getT2()))
                        .then(Mono.just(this)),
                in -> Mono.fromRunnable(() -> {
                    try
                    {
                        in.close();
                    }
                    catch(IOException e)
                    {
                        throw new RuntimeException(e);
                    }
                }).subscribeOn(Schedulers.boundedElastic()));
    }

    @Override
    public Mono<Void> handle()
    {
        final var assets = Application.assets();

        return Mono.just(this.container)
                .flatMap(AssetContainer::initialize)
                .flatMap(container -> {
                    if(!sheetExceptions.isEmpty())
                    {
                        // Issue comment 로 Sheet 예외 사항 출력하고 더 이상 진행하지 않음
                        return behavior.commentExceptions(projectId, issueId, sheetExceptions)
                                .then(Mono.error(new RuntimeException(Application.messageSourceAccessor().getMessage("EXCEPTION_STEP_READ_SHEET")
                                        .formatted(commitId))));
                    }

                    return Mono.fromSupplier(() -> assets.verifier(container))
                            .flatMap(verifier -> verifier.exceptions(behavior, projectId, issueId, commitId).collectList())
                            .flatMap(constraintsExceptions -> {
                                if(!constraintsExceptions.isEmpty())
                                {
                                    // Issue comment 로 데이터 무결성 검사 에러 출력하고 더 이상 진행하지 않음
                                    return behavior.commentExceptions(projectId, issueId, constraintsExceptions)
                                            .then(Mono.error(new RuntimeException(Application.messageSourceAccessor().getMessage("EXCEPTION_STEP_CONSTRAINTS")
                                                    .formatted(constraintsExceptions.size()))));
                                }

                                return Mono.empty();
                            });
                });
    }

    @Override
    @Transactional
    public Mono<Void> complete()
    {
        final var repository = Application.repository(DataRepository.class);

        return Flux.fromIterable(this.container.updatedData())
                .flatMap(tuple -> {
                    final var vaults = Application.vaults();
                    final var data = tuple.getT1();
                    final var archive = tuple.getT2();

                    return vaults.upload(data.getSheetName(), archive)
                            .flatMap(key -> {
                                data.setSha256(key);

                                // TODO 이미 동일한 레코드 등록 시도로 에러 발생함
                                return repository.save(data)
                                        .onErrorResume(error -> {
                                            final var dataSize = DataSize.ofBytes(archive.toByteArray().length);

                                            return behavior.commentExceptions(projectId, issueId, error)
                                                    .then(Mono.error(new RuntimeException(Application.messageSourceAccessor()
                                                            .getMessage("EXCEPTION_STEP_SAVE_DATA")
                                                            .formatted(data.getCommitId(), data.getScope(), data.getName(), dataSize.toMegabytes()))));
                                        });
                            });
                })
                .then();
    }

    private Mono<Tuple2<Data, Archive>> toData(final String commitId, final String packageName, final AssetSheet sheet, final Scope scope,
                                               final List<Throwable> exceptionContainer)
    {
        return Mono.using(() -> sheet,
                s -> {
                    if(s.isEmpty(scope))
                        return Mono.empty();

                    final var data = new Data();
                    data.setCommitId(commitId);
                    data.setName(sheet.name());
                    data.setScope(scope);

                    final var schemaSerializer = new ProtobufSchemaSerializer(packageName, scope);
                    final var sheetCodec = new ProtobufRowCodec(packageName, sheet, scope);
                    final var linkCodec = new AssetLinkCodec();
                    final var archive = Archive.newBuilder()
                            .setSchema(schemaSerializer.serialize(sheet))
                            .setDescriptorBytes(sheetCodec.getFileDescriptor().toProto().toByteString())
                            .setDataBytes(ByteString.copyFrom(sheetCodec.serialize(sheet)))
                            .setLinkBytes(ByteString.copyFrom(linkCodec.serialize(sheet)))
                            .setNullableBytes(sheetCodec.nullableFields().toByteString())
                            .build();

                    exceptionContainer.addAll(sheet.exceptions());

                    if(!exceptionContainer.isEmpty())
                    {
                        exceptionContainer.forEach(e -> e.printStackTrace(System.out));
                        return Mono.empty();
                    }

                    return Mono.just(Tuples.of(data, archive));
                },
                AssetSheet::close);
    }
}
