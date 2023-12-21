package boozilla.houston.grpc.webhook.handler;

import boozilla.houston.grpc.webhook.GitBehavior;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public abstract class GitFileHandler {
    protected final long projectId;
    protected final long issueId;
    protected final String commitId;
    protected final String packageName;
    protected final GitBehavior<?> behavior;

    public abstract Mono<? extends GitFileHandler> add(final String path, final byte[] bytes);

    public abstract Mono<Void> handle();

    @Transactional
    public abstract Mono<Void> complete();
}
