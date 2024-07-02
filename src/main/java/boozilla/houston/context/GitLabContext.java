package boozilla.houston.context;

import boozilla.houston.common.Retry;
import com.linecorp.armeria.server.ServiceRequestContext;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.Constants;
import org.gitlab4j.api.GitLabApi;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Objects;
import java.util.function.Function;

@Slf4j
public class GitLabContext implements GitContext<GitLabApi> {
    private static final String GITLAB_ACCESS_TOKEN_KEY = "x-gitlab-token";
    private static final String GITLAB_URL_KEY = "x-gitlab-instance";

    private final String url;
    private final ServiceRequestContext context;

    private GitLabContext(final String url, final ServiceRequestContext context)
    {
        this.url = url;
        this.context = context;
    }

    public static GitLabContext of(final String url, final ServiceRequestContext context)
    {
        return new GitLabContext(url, context);
    }

    public static GitLabContext current(final String url)
    {
        return new GitLabContext(url, ServiceRequestContext.current());
    }

    // Request context 안에서만 호출돼야 한다.
    private String accessToken()
    {
        return header(GITLAB_ACCESS_TOKEN_KEY, "Access token is null");
    }

    private String url()
    {
        final var headerUrl = header(GITLAB_URL_KEY, "GitLab URL is null");

        if(!headerUrl.equals(url))
            throw new IllegalArgumentException("GitLab URL is not matched");

        return url;
    }

    private String header(final String key, final String message)
    {
        final var value = context.request()
                .headers()
                .get(key);

        return Objects.requireNonNull(value, message);
    }

    public <R> Mono<R> api(final Function<GitLabApi, Mono<R>> function)
    {
        return Mono.using(
                        () -> new GitLabApi(url(), Constants.TokenType.ACCESS, accessToken()),
                        function,
                        GitLabApi::close
                )
                .retryWhen(Retry.defaultBackoff())
                .onErrorMap(Exceptions::isRetryExhausted, Throwable::getCause)
                .onErrorStop()
                .doOnError(error -> log.error("Errors in GitLab API requests", error))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
