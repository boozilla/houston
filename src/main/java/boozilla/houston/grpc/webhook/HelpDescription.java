package boozilla.houston.grpc.webhook;

import boozilla.houston.grpc.webhook.command.Command;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class HelpDescription {
    private MessageSourceAccessor messageAccessor;
    private List<Command> commands;

    public String description()
    {
        return """
                %s
                
                ## %s
                
                %s
                
                <table>
                <tr>
                <th>%s</th>
                <th>%s</th>
                <th>%s</th>
                %s
                </tr>
                </table>
                """
                .formatted(helpDescription(),
                        messageAccessor.getMessage("COMMAND_TITLE"),
                        messageAccessor.getMessage("COMMAND_SUB_TITLE"),
                        messageAccessor.getMessage("COMMAND_TABLE_HEADER_COMMAND"),
                        messageAccessor.getMessage("COMMAND_TABLE_HEADER_EXPLAIN"),
                        messageAccessor.getMessage("COMMAND_TABLE_HEADER_OPTION"),
                        commands.stream()
                                .map(this::commandDescription)
                                .reduce(Strings.EMPTY, (a, b) -> a + b));
    }

    private String commandDescription(final Command command)
    {
        return """
                <tr>
                <td>%s</td>
                <td>%s</td>
                <td>
                %s
                
                <br>
                
                %s
                </td>
                </tr>
                """
                .formatted(String.join("\n", command.aliases()),
                        messageAccessor.getMessage(command.description()),
                        messageAccessor.getMessage(command.options()),
                        messageAccessor.getMessage(example(command)));
    }

    private String example(final Command command)
    {
        if(!command.example().isEmpty())
        {
            return command.example().stream()
                    .map(code -> "> " + messageAccessor.getMessage(code))
                    .collect(Collectors.joining("\n>\n"));
        }

        return Strings.EMPTY;
    }

    private String helpDescription()
    {
        return """
                ## %s
                
                %s
                %s
                """
                .formatted(messageAccessor.getMessage("HELP_TITLE"),
                        messageAccessor.getMessage("HELP_ENVIRONMENT_BRANCH"),
                        messageAccessor.getMessage("HELP_PARTITION"));
    }
}
