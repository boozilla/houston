package boozilla.houston.console;

import boozilla.houston.Application;
import boozilla.houston.entity.Data;
import boozilla.houston.grpc.webhook.client.console.ConsoleBehavior;
import boozilla.houston.grpc.webhook.command.Commands;
import boozilla.houston.repository.DataRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class ConsoleRepl {
    private final Commands commands;
    private final ConsoleBehavior behavior;
    private final String packageName;
    private final String targetBranch;
    private final String projectId;
    private final DataRepository dataRepository;

    public void run()
    {
        log.info("Console REPL started. Type /help for available commands, /quit to exit.");

        try(final var reader = new BufferedReader(new InputStreamReader(System.in)))
        {
            while(true)
            {
                System.out.print("houston> ");
                System.out.flush();

                final var line = reader.readLine();

                if(line == null)
                {
                    break;
                }

                final var input = line.trim();

                if(input.isEmpty())
                {
                    continue;
                }

                if(input.equals("/quit") || input.equals("/exit"))
                {
                    log.info("Console REPL exited.");
                    break;
                }

                if(input.startsWith("/apply") || input.startsWith("/revoke"))
                {
                    if(!preprocessApplyRevoke(input, behavior))
                    {
                        continue;
                    }
                }

                final var command = commands.find(input);

                if(command.isEmpty())
                {
                    log.warn("Unknown command: {}", input);
                    continue;
                }

                try
                {
                    command.get()
                            .run(packageName, projectId, "0", targetBranch, input, behavior)
                            .block();
                }
                catch(final Exception e)
                {
                    log.error("Command execution failed: {}", e.getMessage(), e);
                }
            }
        }
        catch(final Exception e)
        {
            log.error("Console REPL error", e);
        }
    }

    private boolean preprocessApplyRevoke(final String input, final ConsoleBehavior behavior)
    {
        final var messageSourceAccessor = Application.messageSourceAccessor();
        final var dataFlux = input.startsWith("/apply")
                ? dataRepository.findByApplyAtIsNull()
                : dataRepository.findByApplyAtIsNotNull();
        final var promptTitle = input.startsWith("/apply")
                ? messageSourceAccessor.getMessage("CONSOLE_COMMIT_TITLE_UNAPPLIED")
                : messageSourceAccessor.getMessage("CONSOLE_COMMIT_TITLE_APPLIED");

        final var dataList = dataFlux.collectList().block();
        final var grouped = dataList.stream()
                .collect(Collectors.groupingBy(
                        d -> d.getCommitId().substring(0, Math.min(8, d.getCommitId().length())),
                        LinkedHashMap::new,
                        Collectors.toList()));

        final var items = grouped.entrySet().stream()
                .map(e -> new CheckboxPrompt.Item(
                        e.getKey(),
                        e.getValue().stream().map(Data::getSheetName)
                                .distinct().collect(Collectors.joining(", "))
                                + " (" + messageSourceAccessor.getMessage("CONSOLE_COMMIT_COUNT")
                                        .formatted(e.getValue().size()) + ")"))
                .toList();

        if(items.isEmpty())
        {
            log.info(messageSourceAccessor.getMessage("CONSOLE_COMMIT_EMPTY"));
            return false;
        }

        final var selectedIndices = CheckboxPrompt.show(promptTitle, items);
        if(selectedIndices.isEmpty())
        {
            return false;
        }

        final var commitIds = new ArrayList<>(grouped.keySet());
        final var selectedCommitIds = selectedIndices.stream()
                .map(commitIds::get)
                .toList();
        behavior.setLabels(selectedCommitIds);
        return true;
    }
}
