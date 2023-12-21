package boozilla.houston.grpc.webhook.command;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
public class Commands {
    private final List<StereotypeCommand> commands;

    public Optional<StereotypeCommand> find(final String input)
    {
        return commands.stream()
                .filter(cmd -> cmd.aliases().stream().anyMatch(input::startsWith))
                .findFirst();
    }
}
