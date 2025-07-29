package boozilla.houston.unframed;

import boozilla.houston.annotation.SecuredService;
import boozilla.houston.decorator.auth.JwtAdminAuthorizer;
import boozilla.houston.grpc.webhook.client.gitlab.GitLabBehavior;
import boozilla.houston.grpc.webhook.command.Commands;
import boozilla.houston.grpc.webhook.command.PayloadCommand;
import boozilla.houston.unframed.request.gitlab.NoteEvent;
import boozilla.houston.unframed.request.gitlab.PushEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.annotation.MatchesHeader;
import com.linecorp.armeria.server.annotation.Post;
import com.linecorp.armeria.server.annotation.ProducesJson;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.stream.Collectors;

@ProducesJson
@SecuredService(JwtAdminAuthorizer.class)
public class GitLabService implements UnframedService {
    private final Commands commands;
    private final String targetBranch;
    private final String packageName;
    private final ObjectMapper objectMapper;

    public GitLabService(final Commands commands,
                         @Value("${branch}") final String targetBranch,
                         @Value("${package-name}") final String packageName,
                         final ObjectMapper objectMapper)
    {
        this.commands = commands;
        this.targetBranch = targetBranch;
        this.packageName = packageName;
        this.objectMapper = objectMapper;
    }

    @Post("/gitlab/webhook")
    @MatchesHeader("x-gitlab-event=Push Hook")
    public Mono<Void> push(final PushEvent request)
    {
        final var behavior = new GitLabBehavior(ServiceRequestContext.current(), objectMapper);
        final var requestBranch = behavior.branchFromRef(request.ref());

        if(targetBranch.equalsIgnoreCase(requestBranch))
        {
            final var projectId = request.projectId();

            behavior.openedIssues(projectId)
                    .flatMap(linkedIssues -> Flux.fromIterable(linkedIssues)
                            .flatMap(i -> behavior.findUploadPayload(projectId, i.getId())
                                    .map(PayloadCommand::decode)
                                    .flatMapMany(payload -> Flux.fromIterable(payload.getCommitFileList())))
                            .collect(Collectors.toUnmodifiableSet())
                            .flatMap(additionalFiles -> behavior.uploadPayload(projectId,
                                            request.userId(),
                                            request.ref(),
                                            request.before(),
                                            request.after(),
                                            additionalFiles)
                                    .flatMap(uploadPayload -> behavior.createIssue(uploadPayload)
                                            .flatMap(issue -> behavior.linkIssues(projectId, issue.getId(), linkedIssues)
                                                    .then(behavior.commentUploadPayload(issue.getId(), uploadPayload))))
                            ))
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe();
        }

        return Mono.empty();
    }

    @Post("/gitlab/webhook")
    @MatchesHeader("x-gitlab-event=Note Hook")
    public Mono<Void> note(final NoteEvent request)
    {
        final var behavior = new GitLabBehavior(ServiceRequestContext.current(), objectMapper);

        Mono.just(request)
                .filter(req -> req.issue().labels()
                        .stream()
                        .anyMatch(label -> label.title().equalsIgnoreCase(this.targetBranch)))
                .flatMap(req -> {
                    final var projectId = req.project().id();
                    final var issueIid = req.issue().iid();
                    final var note = req.objectAttributes().note();
                    final var command = commands.find(note);

                    if(command.isPresent())
                    {
                        return command.get().run(this.packageName, projectId, issueIid, this.targetBranch, note, behavior);
                    }

                    return Mono.empty();
                })
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();

        return Mono.empty();
    }
}
