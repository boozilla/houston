package boozilla.houston.grpc.webhook.client.github;

import boozilla.houston.grpc.webhook.GitBehavior;
import boozilla.houston.grpc.webhook.StateLabel;
import boozilla.houston.grpc.webhook.client.Issue;
import com.linecorp.armeria.server.ServiceRequestContext;
import houston.vo.webhook.UploadPayload;
import org.joda.time.Period;
import reactor.core.publisher.Mono;

import java.util.List;

public class GitHubBehavior implements GitBehavior<GitHubClient> {
    public GitHubBehavior(final ServiceRequestContext context)
    {

    }

    @Override
    public GitHubClient client()
    {
        return null;
    }

    @Override
    public Mono<UploadPayload> uploadPayload(final String projectId, final long assignee, final String ref, final String beforeCommitId, final String afterCommitId)
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
    public Mono<Void> linkIssues(final String issueId, final UploadPayload uploadPayload)
    {
        return Mono.empty();
    }

    @Override
    public Mono<Void> setState(final String projectId, final String issueId, final StateLabel newLabel)
    {
        return Mono.empty();
    }

    @Override
    public Mono<Void> addLabels(final String projectId, final String issueId, final String... labels)
    {
        return Mono.empty();
    }

    @Override
    public Mono<Void> commentMessage(final String projectId, final String issueIid, final String message)
    {
        return Mono.empty();
    }

    @Override
    public Mono<Void> commentExceptions(final String projectId, final String issueId, final Throwable exception)
    {
        return Mono.empty();
    }

    @Override
    public Mono<Void> commentExceptions(final String projectId, final String issueId, final List<Throwable> exceptions)
    {
        return Mono.empty();
    }

    @Override
    public Mono<Void> commentUploadPayload(final String issueId, final UploadPayload uploadPayload)
    {
        return Mono.empty();
    }

    @Override
    public Mono<byte[]> openFile(final String projectId, final String ref, final String path)
    {
        return Mono.empty();
    }

    @Override
    public Mono<String> findUploadPayload(final String projectId, final String issueIid)
    {
        return Mono.empty();
    }

    @Override
    public Mono<List<String>> allFiles(final String projectId, final String ref)
    {
        return Mono.empty();
    }

    @Override
    public Mono<String> commitId(final String projectId, final String ref)
    {
        return Mono.empty();
    }

    @Override
    public Mono<Void> addSpentTime(final String projectId, final String issueId, final Period period)
    {
        return Mono.empty();
    }

    @Override
    public Mono<Void> closeIssue(final String projectId, final String issueId)
    {
        return Mono.empty();
    }
}
