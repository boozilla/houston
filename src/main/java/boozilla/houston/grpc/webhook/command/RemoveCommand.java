package boozilla.houston.grpc.webhook.command;

import boozilla.houston.grpc.webhook.GitBehavior;
import boozilla.houston.repository.DataRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuples;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

@Component
@AllArgsConstructor
public class RemoveCommand implements Command {
    private final Pattern sheetNamePattern = Pattern.compile("('[^']*'|\"[^\"]*\"|\\w+)");
    private final MessageSourceAccessor messageSourceAccessor;
    private final DataRepository dataRepository;

    @Override
    public Set<String> aliases()
    {
        return Set.of("/remove");
    }

    @Override
    public String commandTemplate()
    {
        return "/remove";
    }

    @Override
    public Mono<Void> run(final String packageName, final long projectId, final long issueId,
                          final String targetRef, final String command, final GitBehavior<?> behavior)
    {
        final var args = command.split(" ", 2);

        if(args.length < 2)
            return Mono.empty();

        final var matcher = sheetNamePattern.matcher(args[1]);
        final var sheetNames = new HashSet<String>();

        while(matcher.find())
        {
            sheetNames.add(matcher.group(1)
                    .replaceAll("^['\"]|['\"]$", ""));
        }

        return Flux.fromIterable(sheetNames)
                .flatMap(sheetName -> dataRepository.deleteByNameStartsWith(sheetName + "%")
                        .map(affected -> Tuples.of(sheetName, affected)))
                .collectList()
                .map(affected -> {
                    final var removed = new HashSet<String>();
                    final var notRemoved = new HashSet<String>();

                    affected.forEach(tuple -> {
                        if(tuple.getT2() > 0)
                            removed.add(tuple.getT1());
                        else
                            notRemoved.add(tuple.getT1());
                    });

                    final var messages = new ArrayList<String>();

                    if(!removed.isEmpty())
                    {
                        messages.add(messageSourceAccessor.getMessage("REMOVE_COMMAND_REMOVED_SHEET")
                                .formatted(String.join(", ", removed)));
                    }

                    if(!notRemoved.isEmpty())
                    {
                        messages.add(messageSourceAccessor.getMessage("REMOVE_COMMAND_AFFECTED_NOTHING")
                                .formatted(String.join(", ", notRemoved)));
                    }

                    return String.join("<br>", messages);
                })
                .doOnNext(message -> behavior.commentMessage(projectId, issueId, message)
                        .subscribeOn(Schedulers.boundedElastic())
                        .subscribe())
                .then();
    }

    @Override
    public String description()
    {
        return "HELP_COMMAND_REMOVE_DESCRIPTION";
    }

    @Override
    public String options()
    {
        return "HELP_COMMAND_REMOVE_OPTIONS";
    }

    @Override
    public Set<String> example()
    {
        return Set.of("HELP_COMMAND_REMOVE_EXAMPLE_1");
    }
}
