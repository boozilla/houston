package boozilla.houston.grpc.webhook.client.gitlab;

import boozilla.houston.annotation.SecuredService;
import boozilla.houston.grpc.webhook.command.Commands;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Empty;
import com.linecorp.armeria.server.ServiceRequestContext;
import houston.grpc.webhook.ReactorGitLabGrpc;
import houston.vo.webhook.gitlab.NoteEvent;
import houston.vo.webhook.gitlab.PushEvent;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@SecuredService
public class GitLabGrpc extends ReactorGitLabGrpc.GitLabImplBase {
    private final Commands commands;
    private final String targetBranch;
    private final String packageName;
    private final ObjectMapper objectMapper;

    public GitLabGrpc(final Commands commands,
                      @Value("${branch}") final String targetBranch,
                      @Value("${package-name}") final String packageName,
                      final ObjectMapper objectMapper)
    {
        this.commands = commands;
        this.targetBranch = targetBranch;
        this.packageName = packageName;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Empty> push(final PushEvent request)
    {
        final var behavior = new GitLabBehavior(ServiceRequestContext.current(), objectMapper);

        behavior.uploadPayload(request.getProjectId(), request.getUserId(),
                        request.getRef(), request.getBefore(), request.getAfter())
                .flatMap(uploadPayload -> behavior.createIssue(uploadPayload)
                        .flatMap(issue -> behavior.linkIssues(issue.getIid(), uploadPayload)
                                .and(behavior.commentUploadPayload(issue.getIid(), uploadPayload))))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();

        return Mono.just(Empty.getDefaultInstance());
    }

    @Override
    public Mono<Empty> note(final Mono<NoteEvent> request)
    {
        final var behavior = new GitLabBehavior(ServiceRequestContext.current(), objectMapper);

        request.filter(req -> req.getIssue().getLabelsList()
                        .stream()
                        .anyMatch(label -> label.getTitle().equalsIgnoreCase(this.targetBranch)))
                .flatMap(req -> {
                    final var projectId = req.getProject().getId();
                    final var issueIid = req.getIssue().getIid();
                    final var note = req.getObjectAttributes().getNote();
                    final var command = commands.find(note);

                    if(command.isPresent())
                    {
                        return command.get().run(this.packageName, projectId, issueIid, this.targetBranch, note, behavior);
                    }

                    return Mono.empty();
                })
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();

        return Mono.just(Empty.getDefaultInstance());
    }
}
