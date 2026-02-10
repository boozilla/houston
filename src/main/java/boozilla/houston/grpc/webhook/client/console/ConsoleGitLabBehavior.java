package boozilla.houston.grpc.webhook.client.console;

import boozilla.houston.grpc.webhook.GitBehavior;
import boozilla.houston.grpc.webhook.StateLabel;
import boozilla.houston.grpc.webhook.client.Issue;
import boozilla.houston.grpc.webhook.client.gitlab.GitLabClient;
import boozilla.houston.grpc.webhook.client.gitlab.repository.RepositoryTreeResponse;
import houston.vo.webhook.UploadPayload;
import lombok.AllArgsConstructor;
import org.joda.time.Period;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

@AllArgsConstructor
public class ConsoleGitLabBehavior implements GitBehavior<ConsoleClient> {
    private final GitLabClient gitLabClient;

    @Override
    public ConsoleClient client()
    {
        return new ConsoleClient();
    }

    @Override
    public Mono<UploadPayload> uploadPayload(final String projectId, final String assignee,
                                              final String ref, final String beforeCommitId,
                                              final String afterCommitId,
                                              final Set<String> additionalFiles)
    {
        return Mono.empty();
    }

    @Override
    public Mono<Issue> createIssue(final UploadPayload uploadPayload)
    {
        return Mono.empty();
    }

    @Override
    public Mono<Issue> getIssue(final String projectId, final String issueId)
    {
        return Mono.empty();
    }

    @Override
    public Mono<Void> linkIssues(final String projectId, final String issueId,
                                  final List<Issue> linkedIssue)
    {
        return Mono.empty();
    }

    @Override
    public Mono<Void> setState(final String projectId, final String issueId,
                                final StateLabel newLabel)
    {
        return Mono.empty();
    }

    @Override
    public Mono<Void> addLabels(final String projectId, final String issueId,
                                 final String... labels)
    {
        return Mono.empty();
    }

    @Override
    public Mono<Void> commentMessage(final String projectId, final String issueIid,
                                      final String message)
    {
        return Mono.empty();
    }

    @Override
    public Mono<Void> commentExceptions(final String projectId, final String issueId,
                                         final Throwable exception)
    {
        return Mono.empty();
    }

    @Override
    public Mono<Void> commentExceptions(final String projectId, final String issueId,
                                         final List<Throwable> exceptions)
    {
        return Mono.empty();
    }

    @Override
    public Mono<Void> commentUploadPayload(final String issueId,
                                            final UploadPayload uploadPayload)
    {
        return Mono.empty();
    }

    @Override
    public Mono<byte[]> openFile(final String projectId, final String ref, final String path)
    {
        return gitLabClient.getRawFile(projectId, ref, path, false);
    }

    @Override
    public Mono<String> findUploadPayload(final String projectId, final String issueIid)
    {
        return Mono.empty();
    }

    @Override
    public Mono<List<String>> allFiles(final String projectId, final String ref)
    {
        return gitLabClient.tree(projectId, "/", ref, true)
                .filter(item -> item.type().contentEquals("blob"))
                .map(RepositoryTreeResponse.Node::path)
                .collectList();
    }

    @Override
    public Mono<String> commitId(final String projectId, final String ref)
    {
        return gitLabClient.getBranches(projectId, ref)
                .map(branch -> branch.commit().id())
                .defaultIfEmpty(ref);
    }

    @Override
    public Mono<Void> addSpentTime(final String projectId, final String issueId,
                                    final Period period)
    {
        return Mono.empty();
    }

    @Override
    public Mono<Void> closeIssue(final String projectId, final String issueId)
    {
        return Mono.empty();
    }
}
