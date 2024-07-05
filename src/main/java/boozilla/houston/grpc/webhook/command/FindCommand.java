package boozilla.houston.grpc.webhook.command;

import boozilla.houston.asset.AssetInputStream;
import boozilla.houston.asset.AssetReader;
import boozilla.houston.grpc.webhook.GitBehavior;
import boozilla.houston.grpc.webhook.handler.Extension;
import lombok.AllArgsConstructor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class FindCommand implements Command {
    private final MessageSourceAccessor messageSourceAccessor;

    @Override
    public Set<String> aliases()
    {
        return Set.of("/find");
    }

    @Override
    public String commandTemplate()
    {
        return "/find %s";
    }

    @Override
    public Mono<Void> run(final String packageName, final String projectId, final String issueId,
                          final String targetRef, final String command, final GitBehavior<?> behavior)
    {
        final var found = Arrays.stream(command.split(" "))
                .map(e -> e.replaceAll(",", ""))
                .map(String::trim)
                .skip(1)
                .collect(Collectors.toMap(Function.identity(), name -> new HashSet<String>()));

        final var findStartMessage = messageSourceAccessor.getMessage("FIND_COMMAND_START_MESSAGE")
                .formatted(found.keySet().stream()
                                .map("`%s`"::formatted)
                                .collect(Collectors.joining(" ")),
                        found.size());

        return behavior.commentMessage(projectId, issueId, findStartMessage)
                .and(behavior.allFiles(projectId, targetRef)
                        .flatMapMany(files -> Flux.fromIterable(files)
                                .flatMap(filename -> {
                                    final var extension = Extension.of(filename);

                                    if(extension != Extension.XLSX_ASSET_WORKBOOK)
                                    {
                                        return Flux.empty();
                                    }

                                    return behavior.openFile(projectId, targetRef, filename)
                                            .flatMap(bytes -> Mono.usingWhen(AssetInputStream.open(filename, bytes),
                                                    in -> AssetReader.of(in)
                                                            .flatMapMany(AssetReader::sheets)
                                                            .doOnNext(sheet -> {
                                                                if(found.containsKey(sheet.name()))
                                                                {
                                                                    found.get(sheet.name()).add(filename);
                                                                }
                                                            })
                                                            .then(),
                                                    in -> Mono.fromRunnable(() -> {
                                                        try
                                                        {
                                                            in.close();
                                                        }
                                                        catch(IOException e)
                                                        {
                                                            throw new RuntimeException(e);
                                                        }
                                                    }).subscribeOn(Schedulers.boundedElastic())));
                                })))
                .thenMany(Flux.fromIterable(found.entrySet())
                        .flatMap(entry -> {
                            final var locations = entry.getValue();

                            if(locations.isEmpty())
                            {
                                return behavior.commentMessage(projectId, issueId, messageSourceAccessor.getMessage("FIND_COMMAND_NOT_FOUND_MESSAGE")
                                        .formatted(entry.getKey()));
                            }
                            else
                            {
                                return Flux.fromIterable(locations)
                                        .flatMap(location -> behavior.commentMessage(projectId, issueId, messageSourceAccessor.getMessage("FIND_COMMAND_FOUND_MESSAGE")
                                                .formatted(entry.getKey(), location)));
                            }
                        }))
                .then();
    }

    @Override
    public String description()
    {
        return "HELP_COMMAND_FIND_DESCRIPTION";
    }

    @Override
    public String options()
    {
        return "HELP_COMMAND_FIND_OPTIONS";
    }

    @Override
    public Set<String> example()
    {
        return Set.of("HELP_COMMAND_FIND_EXAMPLE_1");
    }
}
