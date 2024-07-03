package boozilla.houston.grpc.webhook.command;

import boozilla.houston.grpc.webhook.GitBehavior;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class InitCommand implements Command {
    @Override
    public Set<String> aliases()
    {
        return Set.of("/init");
    }

    @Override
    public String commandTemplate()
    {
        return "/init";
    }

    @Override
    public Mono<Void> run(final String packageName, final String projectId, final String issueId,
                          final String targetRef, final String command, final GitBehavior<?> behavior)
    {
        return behavior.allFiles(projectId, targetRef)
                .map(files -> files.stream().map("'%s'"::formatted)
                        .collect(Collectors.joining(", ")))
                .flatMap(fileArgs -> {
                    final var uploadCommand = getCommand(UploadCommand.class);
                    final var delegateCommand = uploadCommand.commandTemplate()
                            .formatted(targetRef, fileArgs);

                    return uploadCommand.run(packageName, projectId, issueId, targetRef, delegateCommand, behavior);
                });
    }

    @Override
    public String description()
    {
        return "HELP_COMMAND_INIT_DESCRIPTION";
    }

    @Override
    public String options()
    {
        return "HELP_COMMAND_INIT_OPTIONS";
    }

    @Override
    public Set<String> example()
    {
        return Set.of();
    }
}
