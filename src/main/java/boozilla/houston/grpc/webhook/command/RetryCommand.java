package boozilla.houston.grpc.webhook.command;

import boozilla.houston.grpc.webhook.GitBehavior;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Set;

@Component
public class RetryCommand implements Command {
    @Override
    public Set<String> aliases()
    {
        return Set.of("/retry");
    }

    @Override
    public String commandTemplate()
    {
        return "/retry";
    }

    @Override
    public Mono<Void> run(final String packageName, final long projectId, final long issueId,
                          final String targetRef, final String command, final GitBehavior<?> behavior)
    {
        return behavior.findUploadPayload(projectId, issueId)
                .flatMap(uploadPayload -> {
                    final var payloadCommand = getCommand(PayloadCommand.class);
                    return payloadCommand.run(packageName, projectId, issueId, targetRef, uploadPayload, behavior);
                });
    }

    @Override
    public String description()
    {
        return "HELP_COMMAND_RETRY_DESCRIPTION";
    }

    @Override
    public String options()
    {
        return "HELP_COMMAND_RETRY_OPTIONS";
    }

    @Override
    public Set<String> example()
    {
        return Set.of();
    }
}
