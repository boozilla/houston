package boozilla.houston.grpc.webhook.command;

import boozilla.houston.Application;
import boozilla.houston.grpc.webhook.GitBehavior;
import reactor.core.publisher.Mono;

import java.util.Set;

public interface StereotypeCommand {
    Set<String> aliases();

    String commandTemplate();

    Mono<Void> run(final String packageName, final String projectId, final String issueId,
                   final String targetRef, final String command, final GitBehavior<?> behavior);

    default <T extends StereotypeCommand> T getCommand(final Class<T> commandClass)
    {
        return Application.command(commandClass);
    }
}
