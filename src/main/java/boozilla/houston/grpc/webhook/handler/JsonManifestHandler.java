package boozilla.houston.grpc.webhook.handler;

import boozilla.houston.Application;
import boozilla.houston.grpc.webhook.GitBehavior;
import boozilla.houston.repository.ManifestRepository;
import com.google.protobuf.util.JsonFormat;
import houston.grpc.service.Manifest;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class JsonManifestHandler extends GitFileHandler {
    private final List<boozilla.houston.entity.Manifest> manifest;
    private final List<Throwable> exceptions;

    public JsonManifestHandler(final String projectId, final String issueId, final String commitId, final String packageName, final GitBehavior<?> behavior)
    {
        super(projectId, issueId, commitId, packageName, behavior);

        this.manifest = new ArrayList<>();
        this.exceptions = new ArrayList<>();
    }

    @Override
    public Mono<GitFileHandler> add(final String path, final byte[] bytes)
    {
        final var file = new File(path);

        try(final var reader = new InputStreamReader(new ByteArrayInputStream(bytes)))
        {
            final var builder = Manifest.newBuilder();
            JsonFormat.parser()
                    .ignoringUnknownFields()
                    .merge(reader, builder);

            return Mono.fromRunnable(() -> {
                        final var data = builder.build().toByteArray();

                        final var manifest = new boozilla.houston.entity.Manifest();
                        manifest.setName(file.getName().replace(".json", ""));
                        manifest.setCommitId(commitId);
                        manifest.setData(data);

                        this.manifest.add(manifest);
                    })
                    .thenReturn(this);
        }
        catch(IOException e)
        {
            return Mono.fromRunnable(() -> exceptions.add(e))
                    .thenReturn(this);
        }
    }

    @Override
    public Mono<Void> handle()
    {
        if(!this.exceptions.isEmpty())
        {
            // Issue comment 로 Manifest 예외 사항 출력하고 더 이상 진행하지 않음
            return behavior.commentExceptions(projectId, issueId, exceptions)
                    .then(Mono.error(new RuntimeException(Application.messageSourceAccessor().getMessage("EXCEPTION_STEP_READ_MANIFEST")
                            .formatted(commitId))));
        }

        return Mono.empty();
    }

    @Override
    @Transactional
    public Mono<Void> complete()
    {
        final var repository = Application.repository(ManifestRepository.class);

        return Flux.fromIterable(this.manifest)
                .flatMap(manifest -> repository.existsById(manifest.getName())
                        .flatMap(exists -> {
                            if(exists)
                            {
                                manifest.setId(manifest.getName());
                            }

                            return Mono.defer(() -> repository.save(manifest));
                        }))
                .then();
    }
}
