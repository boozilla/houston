package boozilla.houston.unframed;

import boozilla.houston.annotation.SecuredService;
import boozilla.houston.decorator.auth.GitHubAuthorizer;
import boozilla.houston.grpc.webhook.client.github.GitHubBehavior;
import boozilla.houston.grpc.webhook.client.github.GitHubClient;
import boozilla.houston.grpc.webhook.command.Commands;
import boozilla.houston.unframed.request.github.IssueEvent;
import boozilla.houston.unframed.request.github.PushEvent;
import com.google.protobuf.Empty;
import com.linecorp.armeria.server.annotation.Post;
import com.linecorp.armeria.server.annotation.ProducesJson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@ProducesJson
@SecuredService(GitHubAuthorizer.class)
@ConditionalOnBean(GitHubClient.class)
public class GitHubService implements UnframedService {
    private final Commands commands;
    private final String targetBranch;
    private final String packageName;
    private final GitHubBehavior behavior;

    public GitHubService(final Commands commands,
                         @Value("${branch}") final String targetBranch,
                         @Value("${package-name}") final String packageName,
                         final GitHubClient client)
    {
        this.commands = commands;
        this.targetBranch = targetBranch;
        this.packageName = packageName;
        this.behavior = new GitHubBehavior(client);
    }

    @Post("/github/push")
    public Mono<Empty> push(final PushEvent request)
    {
        behavior.uploadPayload(request.repository().fullName(), request.sender().id(),
                        request.ref(), request.before(), request.after())
                .flatMap(uploadPayload -> behavior.createIssue(uploadPayload)
                        .flatMap(issue -> behavior.linkIssues(issue.getIid(), uploadPayload)
                                .and(behavior.commentUploadPayload(issue.getIid(), uploadPayload))))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();

        return Mono.just(Empty.getDefaultInstance());
    }

    @Post("/github/issue")
    public Mono<Void> issue(final IssueEvent request)
    {
        Mono.just(request)
                .filter(req -> req.issue().labels()
                        .stream()
                        .anyMatch(label -> label.name().equalsIgnoreCase(this.targetBranch)))
                .flatMap(req -> {
                    final var projectId = req.repository().fullName();
                    final var issueNumber = req.issue().number();
                    final var body = req.issue().body();
                    final var command = commands.find(body);

                    if(command.isPresent())
                    {
                        return command.get().run(this.packageName, projectId, issueNumber, this.targetBranch, body, behavior);
                    }

                    return Mono.empty();
                })
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();

        return Mono.empty();
    }
}
