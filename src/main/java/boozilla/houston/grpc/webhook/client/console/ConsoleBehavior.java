package boozilla.houston.grpc.webhook.client.console;

import boozilla.houston.console.TerminalRenderer;
import boozilla.houston.grpc.webhook.GitBehavior;
import boozilla.houston.grpc.webhook.StateLabel;
import boozilla.houston.grpc.webhook.client.Issue;
import com.google.common.collect.Lists;
import houston.vo.webhook.UploadPayload;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.Period;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class ConsoleBehavior implements GitBehavior<ConsoleClient> {
    private final ConsoleClient consoleClient = new ConsoleClient();
    private final GitBehavior<?> delegate;
    private final AtomicReference<List<String>> labels = new AtomicReference<>(List.of());

    public ConsoleBehavior(final GitBehavior<?> delegate)
    {
        this.delegate = delegate;
    }

    public void setLabels(final Collection<String> labels)
    {
        this.labels.set(List.copyOf(labels));
    }

    @Override
    public ConsoleClient client()
    {
        return consoleClient;
    }

    @Override
    public Mono<UploadPayload> uploadPayload(final String projectId,
                                              final String assignee,
                                              final String ref,
                                              final String beforeCommitId,
                                              final String afterCommitId,
                                              final Set<String> additionalFiles)
    {
        return delegate.uploadPayload(projectId, assignee, ref, beforeCommitId, afterCommitId, additionalFiles);
    }

    @Override
    public Mono<Issue> createIssue(final UploadPayload uploadPayload)
    {
        log.info("[ISSUE] Created: {}", uploadPayload.getTitle());
        return Mono.just(new ConsoleIssue());
    }

    @Override
    public Mono<Issue> getIssue(final String projectId, final String issueId)
    {
        return Mono.just(new ConsoleIssue(issueId, this.labels.get()));
    }

    @Override
    public Mono<Void> linkIssues(final String projectId, final String issueId, final List<Issue> linkedIssue)
    {
        log.info("[LINK] Issue {} linked to {} issues", issueId, linkedIssue.size());
        return Mono.empty();
    }

    @Override
    public Mono<Void> setState(final String projectId, final String issueId, final StateLabel newLabel)
    {
        log.info("[STATE] Issue {} -> {}", issueId, newLabel);
        return Mono.empty();
    }

    @Override
    public Mono<Void> addLabels(final String projectId, final String issueId, final String... labels)
    {
        return Mono.fromRunnable(() -> {
            log.info("[LABEL] Issue {} + {}", issueId, String.join(", ", labels));
            this.labels.getAndUpdate(current -> {
                final var updated = new ArrayList<>(current);
                updated.addAll(Arrays.asList(labels));
                return List.copyOf(updated);
            });
        });
    }

    @Override
    public Mono<Void> commentMessage(final String projectId, final String issueIid, final String message)
    {
        System.out.println(TerminalRenderer.render(message));
        return Mono.empty();
    }

    @Override
    public Mono<Void> commentExceptions(final String projectId, final String issueId, final Throwable exception)
    {
        return commentExceptions(projectId, issueId, List.of(exception));
    }

    @Override
    public Mono<Void> commentExceptions(final String projectId, final String issueId, final List<Throwable> exceptions)
    {
        return Flux.fromIterable(Lists.partition(exceptions, 10))
                .flatMap(partition -> Flux.fromIterable(partition)
                        .doOnNext(error -> log.error("[ERROR] {}", error.getMessage())))
                .then(setState(projectId, issueId, StateLabel.ERROR));
    }

    @Override
    public Mono<Void> commentUploadPayload(final String issueId, final UploadPayload uploadPayload)
    {
        log.info("[PAYLOAD] Upload payload for issue {}", issueId);
        return Mono.empty();
    }

    @Override
    public Mono<byte[]> openFile(final String projectId, final String ref, final String path)
    {
        return delegate.openFile(projectId, ref, path);
    }

    @Override
    public Mono<String> findUploadPayload(final String projectId, final String issueIid)
    {
        return Mono.empty();
    }

    @Override
    public Mono<List<String>> allFiles(final String projectId, final String ref)
    {
        return delegate.allFiles(projectId, ref);
    }

    @Override
    public Mono<String> commitId(final String projectId, final String ref)
    {
        return delegate.commitId(projectId, ref);
    }

    @Override
    public Mono<Void> addSpentTime(final String projectId, final String issueId, final Period period)
    {
        log.info("[TIME] Issue {} spent {}", issueId, period);
        return Mono.empty();
    }

    @Override
    public Mono<Void> closeIssue(final String projectId, final String issueId)
    {
        log.info("[CLOSE] Issue {}", issueId);
        return Mono.empty();
    }
}
