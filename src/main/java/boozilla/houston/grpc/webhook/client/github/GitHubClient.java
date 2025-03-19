package boozilla.houston.grpc.webhook.client.github;

import boozilla.houston.grpc.webhook.client.GitClient;
import boozilla.houston.properties.GitHubProperties;
import com.linecorp.armeria.client.HttpClient;
import com.linecorp.armeria.client.RestClient;
import com.linecorp.armeria.client.circuitbreaker.CircuitBreaker;
import com.linecorp.armeria.client.circuitbreaker.CircuitBreakerClient;
import com.linecorp.armeria.client.circuitbreaker.CircuitBreakerRule;
import com.linecorp.armeria.client.retry.Backoff;
import com.linecorp.armeria.client.retry.RetryRule;
import com.linecorp.armeria.client.retry.RetryingClient;
import com.linecorp.armeria.common.auth.AuthToken;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
@ConditionalOnProperty(prefix = "github", name = "access-token")
public class GitHubClient implements GitClient {
    private static final String GITHUB_API_URL = "https://api.github.com";
    private static final Function<? super HttpClient, RetryingClient> retryStrategy = retry();
    private static final Function<? super HttpClient, CircuitBreakerClient> circuitBreaker = circuitBreaker();

    private final RestClient restClient;

    public GitHubClient(final GitHubProperties properties)
    {
        restClient = RestClient.builder(GITHUB_API_URL)
                .maxResponseLength(Long.MAX_VALUE)
                .auth(AuthToken.ofOAuth2(properties.accessToken()))
                .followRedirects()
                .decorator(retryStrategy)
                .decorator(circuitBreaker)
                .build();
    }

    // commit compare
    //  https://docs.github.com/ko/rest/dependency-graph/dependency-review?apiVersion=2022-11-28

    // repository file tree
    //  https://docs.github.com/ko/rest/git/trees?apiVersion=2022-11-28

    // download raw file
    //  https://docs.github.com/ko/rest/repos/contents?apiVersion=2022-11-28#get-repository-content

    // get branch commits
    //  https://docs.github.com/ko/rest/branches/branches?apiVersion=2022-11-28#get-a-branch

    // add spent time(not support)

    // create issue
    //  https://docs.github.com/ko/rest/issues/issues?apiVersion=2022-11-28#create-an-issue

    // create issue link
    //  https://docs.github.com/ko/rest/issues/sub-issues?apiVersion=2022-11-28#add-sub-issue

    // get issue
    //  https://docs.github.com/ko/rest/issues/issues?apiVersion=2022-11-28#get-an-issue

    // find issue
    //  https://docs.github.com/ko/rest/issues/issues?apiVersion=2022-11-28#list-repository-issues

    // update issue label
    //  https://docs.github.com/ko/rest/issues/labels?apiVersion=2022-11-28#about-labels

    // close issue
    //  https://docs.github.com/ko/rest/issues/issues?apiVersion=2022-11-28#lock-an-issue

    // write issue comment
    //  https://docs.github.com/ko/rest/issues/comments?apiVersion=2022-11-28#create-an-issue-comment

    // list issue comment
    //  https://docs.github.com/ko/rest/issues/comments?apiVersion=2022-11-28#list-issue-comments

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
}
