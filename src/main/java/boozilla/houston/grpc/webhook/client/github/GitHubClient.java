package boozilla.houston.grpc.webhook.client.github;

import boozilla.houston.grpc.webhook.client.GitClient;
import boozilla.houston.grpc.webhook.client.github.issue.IssueCreateRequest;
import boozilla.houston.grpc.webhook.client.github.issue.IssueGetCommentResponse;
import boozilla.houston.grpc.webhook.client.github.issue.IssueGetResponse;
import boozilla.houston.grpc.webhook.client.github.repository.RepositoryBranchesResponse;
import boozilla.houston.grpc.webhook.client.github.repository.RepositoryCompareResponse;
import boozilla.houston.grpc.webhook.client.github.repository.RepositoryTreesResponse;
import boozilla.houston.properties.GitHubProperties;
import boozilla.houston.repository.vaults.Vaults;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.armeria.client.HttpClient;
import com.linecorp.armeria.client.ResponseAs;
import com.linecorp.armeria.client.RestClient;
import com.linecorp.armeria.client.RestClientPreparation;
import com.linecorp.armeria.client.circuitbreaker.CircuitBreaker;
import com.linecorp.armeria.client.circuitbreaker.CircuitBreakerClient;
import com.linecorp.armeria.client.circuitbreaker.CircuitBreakerRule;
import com.linecorp.armeria.client.retry.Backoff;
import com.linecorp.armeria.client.retry.RetryRule;
import com.linecorp.armeria.client.retry.RetryingClient;
import com.linecorp.armeria.common.HttpEntity;
import com.linecorp.armeria.common.ResponseEntity;
import com.linecorp.armeria.common.auth.AuthToken;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

@Component
@ConditionalOnProperty(prefix = "github", name = "access-token")
public class GitHubClient implements GitClient {
    private static final int DEFAULT_PER_PAGE = 100;
    private static final String GITHUB_API_URL = "https://api.github.com";
    private static final Function<? super HttpClient, RetryingClient> retryStrategy = retry();
    private static final Function<? super HttpClient, CircuitBreakerClient> circuitBreaker = circuitBreaker();

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public GitHubClient(final GitHubProperties properties,
                        final ObjectMapper objectMapper)
    {
        this.restClient = RestClient.builder(GITHUB_API_URL)
                .maxResponseLength(Long.MAX_VALUE)
                .auth(AuthToken.ofOAuth2(properties.accessToken()))
                .followRedirects()
                .decorator(retryStrategy)
                .decorator(circuitBreaker)
                .build();
        this.objectMapper = objectMapper;
    }

    public Mono<RepositoryCompareResponse> compare(final String owner, final String repo, final String base, final String head)
    {
        if(base.contentEquals("0000000000000000000000000000000000000000"))
            return Mono.empty();

        return collect(() -> restClient.get("/repos/{owner}/{repo}/compare/{base}...{head}")
                        .pathParam("owner", owner)
                        .pathParam("repo", repo)
                        .pathParam("base", base)
                        .pathParam("head", head),
                RepositoryCompareResponse.class);
    }

    public Mono<RepositoryTreesResponse> trees(final String owner, final String repo, final String ref, final boolean recursive)
    {
        final var request = restClient.get("/repos/{owner}/{repo}/git/trees/{tree_sha}")
                .pathParam("owner", owner)
                .pathParam("repo", repo)
                .pathParam("tree_sha", ref)
                .queryParam("recursive", recursive)
                .execute(RepositoryTreesResponse.class, objectMapper);

        return Mono.fromFuture(request)
                .map(HttpEntity::content);
    }

    public Mono<byte[]> contents(final String owner, final String repo, final String path, final String ref)
    {
        final var request = restClient.get("/repos/{owner}/{repo}/contents/{path}")
                .header("Accept", "application/vnd.github.raw+json")
                .pathParam("owner", owner)
                .pathParam("repo", repo)
                .pathParam("path", path)
                .queryParam("ref", ref)
                .execute(ResponseAs.bytes());

        return Mono.fromFuture(request)
                .map(HttpEntity::content);
    }

    public Mono<RepositoryBranchesResponse> branches(final String owner, final String repo, final String branch)
    {
        final var request = restClient.get("/repos/{owner}/{repo}/branches/{branch}")
                .pathParam("owner", owner)
                .pathParam("repo", repo)
                .pathParam("branch", branch)
                .execute(RepositoryBranchesResponse.class, objectMapper);

        return Mono.fromFuture(request)
                .map(HttpEntity::content);
    }

    public Mono<IssueGetResponse> createIssue(final String owner, final String repo, final IssueCreateRequest payload)
    {
        final var request = restClient.post("/repos/{owner}/{repo}/issues")
                .pathParam("owner", owner)
                .pathParam("repo", repo)
                .contentJson(payload)
                .execute(IssueGetResponse.class, objectMapper);

        return Mono.fromFuture(request)
                .map(HttpEntity::content);
    }

    public Mono<Void> createSubIssues(final String owner, final String repo, final long issueNumber, final long subIssueNumber)
    {
        final var request = restClient.post("/repos/{owner}/{repo}/issues/{issue_number}/sub_issues")
                .pathParam("owner", owner)
                .pathParam("repo", repo)
                .pathParam("issue_number", issueNumber)
                .contentJson(Map.of("sub_issue_id", subIssueNumber))
                .execute(ResponseAs.bytes());

        return Mono.fromFuture(request)
                .then();
    }

    public Mono<IssueGetResponse> getIssue(final String owner, final String repo, final long issueNumber)
    {
        final var request = restClient.get("/repos/{owner}/{repo}/issues/{issue_number}")
                .pathParam("owner", owner)
                .pathParam("repo", repo)
                .pathParam("issue_number", issueNumber)
                .execute(IssueGetResponse.class, objectMapper);

        return Mono.fromFuture(request)
                .map(HttpEntity::content);
    }

    public Flux<IssueGetResponse> findIssue(final String owner, final String repo, final Set<String> labels)
    {
        return paginate(() -> restClient.get("/repos/{owner}/{repo}/issues")
                        .pathParam("owner", owner)
                        .pathParam("repo", repo)
                        .queryParam("labels", String.join(",", labels))
                        .queryParam("sort", "created")
                        .queryParam("direction", "desc"),
                request -> request.execute(new TypeReference<>() {
                }, objectMapper),
                IssueGetResponse.class);
    }

    private Mono<Void> updateIssue(final String owner, final String repo, final long issueNumber, final Map<String, Object> payload)
    {
        final var request = restClient.patch("/repos/{owner}/{repo}/issues/{issue_number}")
                .pathParam("owner", owner)
                .pathParam("repo", repo)
                .pathParam("issue_number", issueNumber)
                .contentJson(payload)
                .execute(ResponseAs.bytes());

        return Mono.fromFuture(request)
                .then();
    }

    public Mono<Void> updateIssueLabels(final String owner, final String repo, final long issueNumber, final Set<String> labels)
    {
        return updateIssue(owner, repo, issueNumber, Map.of("labels", labels));
    }

    public Mono<Void> closeIssue(final String owner, final String repo, final long issueNumber)
    {
        return updateIssue(owner, repo, issueNumber, Map.of("state", "closed"));
    }

    public Mono<Void> writeIssueComment(final String owner, final String repo, final long issueNumber, final String body)
    {
        final var request = restClient.post("/repos/{owner}/{repo}/issues/{issue_number}/comments")
                .pathParam("owner", owner)
                .pathParam("repo", repo)
                .pathParam("issue_number", issueNumber)
                .contentJson(Map.of("body", body))
                .execute(ResponseAs.bytes());

        return Mono.fromFuture(request)
                .then();
    }

    public Flux<IssueGetCommentResponse> getIssueComments(final String owner, final String repo, final long issueNumber)
    {
        return paginate(() -> restClient.get("/repos/{owner}/{repo}/issues/{issue_number}/comments")
                        .pathParam("owner", owner)
                        .pathParam("repo", repo)
                        .pathParam("issue_number", issueNumber),
                request -> request.execute(new TypeReference<>() {
                }, objectMapper),
                IssueGetCommentResponse.class);
    }

    public Mono<Vaults.UploadResult> createBlob(final String owner, final String repo, final byte[] content)
    {
        final var request = restClient.post("/repos/{owner}/{repo}/git/blobs")
                .pathParam("owner", owner)
                .pathParam("repo", repo)
                .contentJson(Map.of("content", content, "encoding", "base64"))
                .execute(Vaults.UploadResult.class, objectMapper);

        return Mono.fromFuture(request)
                .map(HttpEntity::content);
    }

    public Mono<byte[]> getBlob(final String owner, final String repo, final String sha)
    {
        final var request = restClient.get("/repos/{owner}/{repo}/git/blobs/{file_sha}")
                .header("Accept", "application/vnd.github.raw+json")
                .pathParam("owner", owner)
                .pathParam("repo", repo)
                .pathParam("file_sha", sha)
                .execute(ResponseAs.bytes());

        return Mono.fromFuture(request)
                .map(HttpEntity::content);
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

        final var circuitBreaker = CircuitBreaker.builder("github-client-circuit-breaker")
                .build();

        return CircuitBreakerClient.newDecorator(circuitBreaker, rule);
    }

    private <T extends CollectableResponse<T>> Mono<T> collect(
            final Supplier<RestClientPreparation> requestSupplier,
            final Class<T> resultClass)
    {
        return collect(requestSupplier, resultClass, 1, DEFAULT_PER_PAGE);
    }

    private <T extends CollectableResponse<T>> Mono<T> collect(
            final Supplier<RestClientPreparation> requestSupplier,
            final Class<T> resultClass,
            final int currentPage,
            final int perPage)
    {
        final var request = requestSupplier.get();
        request.queryParam("page", currentPage);
        request.queryParam("per_page", perPage);

        return Mono.fromFuture(request.execute(resultClass, objectMapper))
                .flatMap(response -> {
                    final var linkHeader = response.headers().get("link");
                    final var nextPage = Objects.isNull(linkHeader) || !linkHeader.contains("rel=\"next\"") ?
                            currentPage : currentPage + 1;

                    final var currentContent = response.content();

                    if(nextPage == currentPage)
                    {
                        return Mono.just(currentContent);
                    }

                    return collect(requestSupplier, resultClass, nextPage, perPage)
                            .cast(resultClass)
                            .map(currentContent::accumulate);
                });
    }

    private <T> Flux<T> paginate(
            final Supplier<RestClientPreparation> requestSupplier,
            final Function<RestClientPreparation, CompletableFuture<ResponseEntity<List<T>>>> requestConsumer,
            final Class<T> resultClass)
    {
        return paginate(requestSupplier, requestConsumer, resultClass, 1, DEFAULT_PER_PAGE);
    }

    private <T> Flux<T> paginate(
            final Supplier<RestClientPreparation> requestSupplier,
            final Function<RestClientPreparation, CompletableFuture<ResponseEntity<List<T>>>> requestConsumer,
            final Class<T> resultClass,
            final int currentPage,
            final int perPage)
    {
        final var request = requestSupplier.get();
        request.queryParam("page", currentPage);
        request.queryParam("per_page", perPage);

        return Mono.fromFuture(requestConsumer.apply(request))
                .flatMapMany(response -> {
                    final var linkHeader = response.headers().get("link");
                    final var nextPage = Objects.isNull(linkHeader) || !linkHeader.contains("rel=\"next\"") ?
                            currentPage : currentPage + 1;

                    final var currentContent = response.content();

                    if(nextPage == currentPage)
                    {
                        return Flux.fromIterable(currentContent);
                    }

                    return paginate(requestSupplier, requestConsumer, resultClass, nextPage, perPage)
                            .cast(resultClass);
                });
    }
}
