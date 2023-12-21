package boozilla.houston.grpc.webhook.command;

import boozilla.houston.grpc.webhook.GitBehavior;
import boozilla.houston.grpc.webhook.HelpDescription;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Set;

@Component
@AllArgsConstructor
public class HelpCommand implements StereotypeCommand {
    private final HelpDescription description;

    @Override
    public Set<String> aliases()
    {
        return Set.of("/help");
    }

    @Override
    public String commandTemplate()
    {
        return "/help";
    }

    @Override
    public Mono<Void> run(final String packageName, final long projectId, final long issueId,
                          final String targetRef, final String command, final GitBehavior<?> behavior)
    {
        return behavior.commentMessage(projectId, issueId, description.description());
    }
}
