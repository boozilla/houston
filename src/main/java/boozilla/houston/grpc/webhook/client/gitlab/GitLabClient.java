package boozilla.houston.grpc.webhook.client.gitlab;

import boozilla.houston.grpc.webhook.client.GitClient;
import boozilla.houston.grpc.webhook.client.Issue;
import boozilla.houston.grpc.webhook.client.gitlab.issue.IssueCreateResponse;
import boozilla.houston.grpc.webhook.client.gitlab.issue.IssueGetResponse;
import boozilla.houston.grpc.webhook.client.gitlab.notes.NotesGetResponse;
import boozilla.houston.grpc.webhook.client.gitlab.repository.RepositoryBranchResponse;
import boozilla.houston.grpc.webhook.client.gitlab.repository.RepositoryCompareResponse;
import boozilla.houston.grpc.webhook.client.gitlab.repository.RepositoryTreeResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.linecorp.armeria.client.HttpClient;
import com.linecorp.armeria.client.ResponseAs;
import com.linecorp.armeria.client.RestClient;
import com.linecorp.armeria.client.circuitbreaker.CircuitBreaker;
import com.linecorp.armeria.client.circuitbreaker.CircuitBreakerClient;
import com.linecorp.armeria.client.circuitbreaker.CircuitBreakerRule;
import com.linecorp.armeria.client.retry.Backoff;
import com.linecorp.armeria.client.retry.RetryRule;
import com.linecorp.armeria.client.retry.RetryingClient;
import com.linecorp.armeria.common.HttpEntity;
import com.linecorp.armeria.common.auth.AuthToken;
import com.linecorp.armeria.server.ServiceRequestContext;
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class GitLabClient implements GitClient {
    private static final String GITLAB_ACCESS_TOKEN_KEY = "x-gitlab-token";
    private static final String GITLAB_URL_KEY = "x-gitlab-instance";
    private static final PeriodFormatter periodFormatter = new PeriodFormatterBuilder()
            .appendDays().appendSuffix("d")
            .appendHours().appendSuffix("h")
            .appendMinutes().appendSuffix("m")
            .appendSeconds().appendSuffix("s")
            .toFormatter();

    private final RestClient restClient;

    public GitLabClient(final String url, final String token)
    {
        restClient = RestClient.builder(url)
                .auth(AuthToken.ofOAuth2(token))
                .followRedirects()
                .decorator(retry())
                .decorator(circuitBreaker())
                .build();
    }

    public static GitLabClient of(final ServiceRequestContext context)
    {
        final var token = header(context, GITLAB_ACCESS_TOKEN_KEY, "Access token is null");
        final var url = header(context, GITLAB_URL_KEY, "GitLab URL is null");

        return new GitLabClient(url, token);
    }

    private static String header(final ServiceRequestContext context, final String key, final String message)
    {
        final var value = context.request()
                .headers()
                .get(key);

        return Objects.requireNonNull(value, message);
    }

    private static Function<? super HttpClient, RetryingClient> retry()
    {
        return RetryingClient.newDecorator(RetryRule.failsafe(Backoff.ofDefault()));
    }

    private static Function<? super HttpClient, CircuitBreakerClient> circuitBreaker()
    {
        final var rule = CircuitBreakerRule.builder()
                .onException()
                .thenFailure();

        final var circuitBreaker = CircuitBreaker.builder("gitlab-client-circuit-breaker")
                .build();

        return CircuitBreakerClient.newDecorator(circuitBreaker, rule);
    }

    public URI uri()
    {
        return restClient.uri();
    }

    public Mono<RepositoryCompareResponse> compare(final String projectId, final String from, final String to)
    {
        final var request = restClient.get("/projects/{id}/repository/compare")
                .pathParam("id", projectId)
                .queryParam("from", from)
                .queryParam("to", to)
                .execute(RepositoryCompareResponse.class);

        return Mono.fromFuture(request)
                .map(HttpEntity::content);
    }

    public Flux<RepositoryTreeResponse.Node> tree(final String projectId, final String path, final String ref, final boolean recursive)
    {
        final var request = restClient.get("/projects/{id}/repository/tree")
                .pathParam("id", projectId)
                .queryParam("path", path)
                .queryParam("recursive", recursive)
                .queryParam("ref", ref)
                .execute(new TypeReference<List<RepositoryTreeResponse.Node>>() {
                });

        return Mono.fromFuture(request)
                .flatMapMany(response -> Flux.fromIterable(response.content()));
    }

    public Mono<byte[]> getRawFile(final String projectId, final String filePath, final String ref, final boolean lfs)
    {
        final var request = restClient.get("/projects/{id}/repository/files/{filePath}/raw")
                .pathParam("id", projectId)
                .pathParam("filePath", URLEncoder.encode(filePath, Charset.defaultCharset()))
                .queryParam("ref", ref)
                .queryParam("lfs", lfs)
                .execute(ResponseAs.bytes());

        return Mono.fromFuture(request)
                .map(HttpEntity::content);
    }

    public Mono<RepositoryBranchResponse.Branch> getBranch(final String projectId, final String search)
    {
        final var request = restClient.get("/projects/{id}/repository/branches")
                .pathParam("id", projectId)
                .queryParam("search", search)
                .execute(new TypeReference<List<RepositoryBranchResponse.Branch>>() {
                });

        return Mono.fromFuture(request)
                .flatMap(response -> {
                    final var optBranch = response.content().stream()
                            .findAny();

                    return Mono.justOrEmpty(optBranch);
                });
    }

    public Mono<Void> addSpentTime(final String projectId, final String issueIid, final Duration duration)
    {
        System.out.println(periodFormatter.print(duration.toPeriod()));
        final var request = restClient.post("/projects/{id}/issues/{issueIid}/add_spent_time")
                .pathParam("id", projectId)
                .pathParam("issueIid", issueIid)
                .queryParam("duration", periodFormatter.print(duration.toPeriod()))
                .execute(ResponseAs.bytes());

        return Mono.fromFuture(request)
                .then();
    }

    public Mono<Issue> createIssue(final String projectId, final String title, final String description,
                                   final long assigneeId, final String labels, final ZonedDateTime createdAt)
    {
        final var request = restClient.post("/projects/{id}/issues")
                .pathParam("id", projectId)
                .queryParam("title", title)
                .queryParam("description", description)
                .queryParam("assignee_id", assigneeId)
                .queryParam("labels", labels)
                .queryParam("created_at", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(createdAt))
                .execute(IssueCreateResponse.class);

        return Mono.fromFuture(request)
                .map(HttpEntity::content);
    }

    public Mono<Void> createIssueLink(final String projectId, final String issueIid, final String targetProjectId, final String targetIssueIid)
    {
        final var request = restClient.post("/projects/{id}/issues/{issue_iid}/links")
                .pathParam("id", projectId)
                .pathParam("issue_iid", issueIid)
                .queryParam("target_project_id", targetProjectId)
                .queryParam("target_issue_iid", targetIssueIid)
                .execute(ResponseAs.bytes());

        return Mono.fromFuture(request)
                .then();
    }

    public Mono<Issue> getIssue(final String projectId, final String issueIid)
    {
        final var request = restClient.get("/projects/{id}/issues/{issueIid}")
                .pathParam("id", projectId)
                .pathParam("issueIid", issueIid)
                .execute(IssueGetResponse.class);

        return Mono.fromFuture(request)
                .map(HttpEntity::content);
    }

    public Flux<IssueGetResponse> findIssue(final String projectId, final List<String> labels)
    {
        final var request = restClient.get("/projects/{id}/issues")
                .pathParam("id", projectId)
                .queryParam("labels", String.join(",", labels))
                .execute(new TypeReference<List<IssueGetResponse>>() {
                });

        return Mono.fromFuture(request)
                .flatMapMany(response -> Flux.fromIterable(response.content()));
    }

    public Mono<Void> updateIssueLabel(final String projectId, final String issueIid, final String labels)
    {
        final var request = restClient.put("/projects/{id}/issues/{issueIid}")
                .pathParam("id", projectId)
                .pathParam("issueIid", issueIid)
                .queryParam("labels", labels)
                .execute(ResponseAs.bytes());

        return Mono.fromFuture(request)
                .then();
    }

    public Mono<Void> closeIssue(final String projectId, final String issueIid)
    {
        final var request = restClient.put("/projects/{id}/issues/{issueIid}")
                .pathParam("id", projectId)
                .pathParam("issueIid", issueIid)
                .queryParam("state_event", "close")
                .execute(ResponseAs.bytes());

        return Mono.fromFuture(request)
                .then();
    }

    public Mono<Void> createIssueNote(final String projectId, final String issueIid, final String message)
    {
        final var request = restClient.post("/projects/{id}/issues/{issueIid}/notes")
                .pathParam("id", projectId)
                .pathParam("issueIid", issueIid)
                .queryParam("body", message)
                .execute(ResponseAs.bytes());

        return Mono.fromFuture(request)
                .then();
    }

    public Flux<NotesGetResponse.Note> notes(final String projectId, final String issueIid)
    {
        final var request = restClient.get("/projects/{id}/issues/{issueIid}/notes")
                .pathParam("id", projectId)
                .pathParam("issueIid", issueIid)
                .queryParam("sort", "asc")
                .queryParam("order_by", "created_at")
                .execute(new TypeReference<List<NotesGetResponse.Note>>() {
                });

        return Mono.fromFuture(request)
                .flatMapMany(response -> Flux.fromIterable(response.content()));
    }
}
