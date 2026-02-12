package boozilla.houston.console;

import boozilla.houston.grpc.webhook.client.console.ConsoleBehavior;
import boozilla.houston.grpc.webhook.client.console.ConsoleGitLabBehavior;
import boozilla.houston.grpc.webhook.client.github.GitHubBehavior;
import boozilla.houston.grpc.webhook.client.github.GitHubClient;
import boozilla.houston.grpc.webhook.client.gitlab.GitLabClient;
import boozilla.houston.properties.GitLabProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.armeria.client.ClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@ConditionalOnProperty(name = "console.enabled", havingValue = "true")
public class ConsoleBehaviorFactory {
    private final ObjectMapper objectMapper;
    private final GitLabProperties gitLabProperties;
    private final GitHubClient gitHubClient;
    private final ClientFactory clientFactory;

    public ConsoleBehaviorFactory(final ObjectMapper objectMapper,
                                  final GitLabProperties gitLabProperties,
                                  @Nullable final GitHubClient gitHubClient,
                                  final ClientFactory clientFactory)
    {
        this.objectMapper = objectMapper;
        this.gitLabProperties = gitLabProperties;
        this.gitHubClient = gitHubClient;
        this.clientFactory = clientFactory;
    }

    public Optional<ConsoleBehavior> create()
    {
        if(gitLabProperties != null
                && gitLabProperties.url() != null && !gitLabProperties.url().isBlank()
                && gitLabProperties.accessToken() != null && !gitLabProperties.accessToken().isBlank())
        {
            log.info("Console REPL using GitLab provider: {}", gitLabProperties.url());
            final var gitLabClient = new GitLabClient(gitLabProperties.url(), gitLabProperties.accessToken(), objectMapper, clientFactory);
            return Optional.of(new ConsoleBehavior(new ConsoleGitLabBehavior(gitLabClient)));
        }

        if(gitHubClient != null)
        {
            log.info("Console REPL using GitHub provider");
            return Optional.of(new ConsoleBehavior(new GitHubBehavior(gitHubClient)));
        }

        return Optional.empty();
    }
}
