package boozilla.houston.unframed;

import boozilla.houston.annotation.SecuredService;
import boozilla.houston.decorator.auth.JwtAdminAuthorizer;
import boozilla.houston.grpc.webhook.client.gitlab.GitLabBehavior;
import boozilla.houston.grpc.webhook.command.Commands;
import boozilla.houston.unframed.request.gitlab.NoteEvent;
import boozilla.houston.unframed.request.gitlab.PushEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Empty;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.annotation.Post;
import com.linecorp.armeria.server.annotation.ProducesJson;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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

    @Post("/gitlab/push")
    public Mono<Empty> push(final PushEvent request)
    {
        final var behavior = new GitLabBehavior(ServiceRequestContext.current(), objectMapper);

        behavior.uploadPayload(request.projectId(), request.userId(),
                        request.ref(), request.before(), request.after())
                .flatMap(uploadPayload -> behavior.createIssue(uploadPayload)
                        .flatMap(issue -> behavior.linkIssues(issue.getId(), uploadPayload)
                                .and(behavior.commentUploadPayload(issue.getId(), uploadPayload))))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();

        return Mono.just(Empty.getDefaultInstance());
    }

    @Post("/gitlab/note")
    public Mono<Empty> note(final NoteEvent request)
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

        return Mono.just(Empty.getDefaultInstance());
    }
}
