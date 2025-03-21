package boozilla.houston.grpc.webhook.client.github;

import boozilla.houston.annotation.SecuredService;
import boozilla.houston.grpc.webhook.command.Commands;
import com.google.protobuf.Empty;
import com.linecorp.armeria.server.ServiceRequestContext;
import houston.grpc.webhook.ReactorGitHubGrpc;
import houston.vo.webhook.github.IssueEvent;
import houston.vo.webhook.github.PushEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@SecuredService
@ConditionalOnBean(GitHubClient.class)
public class GitHubGrpc extends ReactorGitHubGrpc.GitHubImplBase {
    private final Commands commands;
    private final String targetBranch;
    private final String packageName;

    public GitHubGrpc(final Commands commands,
                      @Value("${branch}") final String targetBranch,
                      @Value("${package-name}") final String packageName)
    {
        this.commands = commands;
        this.targetBranch = targetBranch;
        this.packageName = packageName;
    }

    @Override
    public Mono<Empty> push(final PushEvent request)
    {
        final var behavior = new GitHubBehavior(ServiceRequestContext.current());

        behavior.uploadPayload(request.getRepository().getId(), request.getSender().getId(),
                        request.getRef(), request.getBefore(), request.getAfter())
                .flatMap(uploadPayload -> behavior.createIssue(uploadPayload)
                        .flatMap(issue -> behavior.linkIssues(issue.getIid(), uploadPayload)
                                .and(behavior.commentUploadPayload(issue.getIid(), uploadPayload))))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();

        return Mono.just(Empty.getDefaultInstance());
    }

    @Override
    public Mono<Empty> issue(final IssueEvent request)
    {
        final var behavior = new GitHubBehavior(ServiceRequestContext.current());

        //        request.filter(req -> req.getIssue().getLabelsList()
        //                        .stream()
        //                        .anyMatch(label -> label.getTitle().equalsIgnoreCase(this.targetBranch)))
        //                .flatMap(req -> {
        //                    final var projectId = req.getProject().getId();
        //                    final var issueIid = req.getIssue().getIid();
        //                    final var note = req.getObjectAttributes().getNote();
        //                    final var command = commands.find(note);
        //
        //                    if(command.isPresent())
        //                    {
        //                        return command.get().run(this.packageName, projectId, issueIid, this.targetBranch, note, behavior);
        //                    }
        //
        //                    return Mono.empty();
        //                })
        //                .subscribeOn(Schedulers.boundedElastic())
        //                .subscribe();

        return Mono.just(Empty.getDefaultInstance());
    }
}
