package boozilla.houston.grpc.webhook.client.gitlab;

import boozilla.houston.common.PeriodFormatter;
import boozilla.houston.grpc.webhook.client.GitClient;
import boozilla.houston.grpc.webhook.client.Issue;
import boozilla.houston.grpc.webhook.client.gitlab.issue.IssueCreateResponse;
import boozilla.houston.grpc.webhook.client.gitlab.issue.IssueGetResponse;
import boozilla.houston.grpc.webhook.client.gitlab.notes.NotesGetResponse;
import boozilla.houston.grpc.webhook.client.gitlab.repository.RepositoryBranchResponse;
import boozilla.houston.grpc.webhook.client.gitlab.repository.RepositoryCompareResponse;
import boozilla.houston.grpc.webhook.client.gitlab.repository.RepositoryTreeResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.linecorp.armeria.common.ResponseEntity;
import com.linecorp.armeria.common.auth.AuthToken;
import com.linecorp.armeria.server.ServiceRequestContext;
import org.joda.time.Period;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
    private static final Function<? super HttpClient, RetryingClient> retryStrategy = retry();
    private static final Function<? super HttpClient, CircuitBreakerClient> circuitBreaker = circuitBreaker();

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public GitLabClient(final String url,
                        final String token,
                        final ObjectMapper objectMapper)
    {
        this.restClient = RestClient.builder("%s/api/v4".formatted(url))
                .maxResponseLength(Long.MAX_VALUE)
                .auth(AuthToken.ofOAuth2(token))
                .followRedirects()
                .decorator(retryStrategy)
                .decorator(circuitBreaker)
                .build();
        this.objectMapper = objectMapper;
    }

    public static GitLabClient of(final ServiceRequestContext context,
                                  final ObjectMapper objectMapper)
    {
        final var token = header(context, GITLAB_ACCESS_TOKEN_KEY, "Access token is null");
        final var url = header(context, GITLAB_URL_KEY, "GitLab URL is null");

        return new GitLabClient(url, token, objectMapper);
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

    public Mono<RepositoryCompareResponse> compare(final String projectId, final String from, final String to)
    {
        final var request = restClient.get("/projects/{id}/repository/compare")
                .pathParam("id", projectId)
                .queryParam("from", from)
                .queryParam("to", to)
                .execute(RepositoryCompareResponse.class, objectMapper);

        return Mono.fromFuture(request)
                .map(HttpEntity::content);
    }

    public Flux<RepositoryTreeResponse.Node> tree(final String projectId, final String path, final String ref, final boolean recursive)
    {
        return tree(projectId, path, ref, recursive, 1);
    }

    public Flux<RepositoryTreeResponse.Node> tree(final String projectId, final String path, final String ref, final boolean recursive, final int page)
    {
        final var request = restClient.get("/projects/{id}/repository/tree")
                .pathParam("id", projectId)
                .queryParam("path", path)
                .queryParam("recursive", recursive)
                .queryParam("ref", ref)
                .queryParam("page", page)
                .queryParam("order_by", "id")
                .queryParam("sort", "asc")
                .queryParam("per_page", 100)
                .execute(ResponseAs.json(new TypeReference<List<RepositoryTreeResponse.Node>>() {
                }, objectMapper));

        return Mono.fromFuture(request)
                .flatMapMany(response -> pagination(response, page, nextPage -> tree(projectId, path, ref, recursive, nextPage)));
    }

    public Mono<byte[]> getRawFile(final String projectId, final String ref, final String filePath, final boolean lfs)
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

    public Mono<RepositoryBranchResponse.Branch> getBranches(final String projectId, final String search)
    {
        final var request = restClient.get("/projects/{id}/repository/branches")
                .pathParam("id", projectId)
                .queryParam("search", search)
                .execute(new TypeReference<List<RepositoryBranchResponse.Branch>>() {
                }, objectMapper);

        return Mono.fromFuture(request)
                .flatMap(response -> {
                    final var optBranch = response.content().stream()
                            .findAny();

                    return Mono.justOrEmpty(optBranch);
                });
    }

    public Mono<Void> addSpentTime(final String projectId, final String issueIid, final Period period)
    {
        final var request = restClient.post("/projects/{id}/issues/{issueIid}/add_spent_time")
                .pathParam("id", projectId)
                .pathParam("issueIid", issueIid)
                .queryParam("duration", PeriodFormatter.print(period))
                .execute(ResponseAs.bytes());

        return Mono.fromFuture(request)
                .then();
    }

    public Mono<Issue> createIssue(final String projectId, final String title, final String description,
                                   final String assigneeId, final String labels, final ZonedDateTime createdAt)
    {
        final var request = restClient.post("/projects/{id}/issues")
                .pathParam("id", projectId)
                .queryParam("title", title)
                .queryParam("description", description)
                .queryParam("assignee_id", assigneeId)
                .queryParam("labels", labels)
                .queryParam("created_at", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(createdAt))
                .execute(IssueCreateResponse.class, objectMapper);

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
                .execute(IssueGetResponse.class, objectMapper);

        return Mono.fromFuture(request)
                .map(HttpEntity::content);
    }

    public Flux<IssueGetResponse> findIssue(final String projectId, final List<String> labels)
    {
        final var request = restClient.get("/projects/{id}/issues")
                .pathParam("id", projectId)
                .pathParam("state", "all")
                .queryParam("labels", String.join(",", labels))
                .execute(new TypeReference<List<IssueGetResponse>>() {
                }, objectMapper);

        return Mono.fromFuture(request)
                .flatMapMany(response -> Flux.fromIterable(response.content()));
    }

    public Flux<IssueGetResponse> findOpenedIssue(final String repo)
    {
        final var request = restClient.get("/projects/{id}/issues")
                .pathParam("id", repo)
                .queryParam("state", "opened")
                .execute(new TypeReference<List<IssueGetResponse>>() {
                }, objectMapper);

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
        return notes(projectId, issueIid, 1);
    }

    public Flux<NotesGetResponse.Note> notes(final String projectId, final String issueIid, final int page)
    {
        final var request = restClient.get("/projects/{id}/issues/{issueIid}/notes")
                .pathParam("id", projectId)
                .pathParam("issueIid", issueIid)
                .queryParam("sort", "asc")
                .queryParam("order_by", "created_at")
                .execute(new TypeReference<List<NotesGetResponse.Note>>() {
                }, objectMapper);

        return Mono.fromFuture(request)
                .flatMapMany(response -> pagination(response, page, nextPage -> notes(projectId, issueIid, nextPage)));
    }

    private <T> Flux<T> pagination(final ResponseEntity<List<T>> response, final int currentPage, final Function<Integer, Flux<T>> next)
    {
        final var nextPageHeader = response.headers().get("x-next-page");
        final var nextPage = Objects.isNull(nextPageHeader) || nextPageHeader.isEmpty() ? currentPage : Integer.parseInt(nextPageHeader);

        if(nextPage == currentPage)
        {
            return Flux.fromIterable(response.content());
        }

        return next.apply(nextPage)
                .mergeWith(Flux.fromIterable(response.content()));
    }
}
