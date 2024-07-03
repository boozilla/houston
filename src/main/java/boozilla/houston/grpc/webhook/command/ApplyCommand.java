package boozilla.houston.grpc.webhook.command;

import boozilla.houston.grpc.webhook.GitBehavior;
import boozilla.houston.grpc.webhook.StateLabel;
import boozilla.houston.repository.DataRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuples;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@AllArgsConstructor
public class ApplyCommand implements Command {
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
    private final MessageSourceAccessor messageSourceAccessor;
    private final DataRepository dataRepository;

    @Override
    public Set<String> aliases()
    {
        return Set.of("/apply");
    }

    @Override
    public String commandTemplate()
    {
        return "/apply";
    }

    @Override
    public Mono<Void> run(final String packageName, final String projectId, final String issueId,
                          final String targetRef, final String command, final GitBehavior<?> behavior)
    {
        final var excludeLabels = Stream.concat(Arrays.stream(StateLabel.values()).map(StateLabel::name), Stream.of(targetRef))
                .collect(Collectors.toUnmodifiableSet());
        final var applyAt = parseApplyTime(command);

        return behavior.getIssue(projectId, issueId)
                .flatMapMany(issue -> Flux.fromIterable(issue.getLabels()))
                .filter(label -> !excludeLabels.contains(label))
                .flatMap(commitId -> dataRepository.updateByCommitIdIsLike(applyAt, commitId + "%")
                        .map(affected -> Tuples.of(commitId, affected)))
                .collectList()
                .flatMap(affected -> {
                    final var applied = new HashSet<String>();
                    final var notApplied = new HashSet<String>();

                    affected.forEach(tuple -> {
                        if(tuple.getT2() > 0)
                            applied.add(tuple.getT1());
                        else
                            notApplied.add(tuple.getT1());
                    });

                    final var messages = new ArrayList<String>();

                    if(!applied.isEmpty())
                    {
                        messages.add(messageSourceAccessor.getMessage("APPLY_COMMAND_APPLIED_COMMIT")
                                .formatted(String.join(", ", applied), applyAt));
                    }

                    if(!notApplied.isEmpty())
                    {
                        messages.add(messageSourceAccessor.getMessage("APPLY_COMMAND_AFFECTED_NOTHING")
                                .formatted(String.join(", ", notApplied)));
                    }

                    return Mono.just(String.join("<br>", messages));
                })
                .doOnNext(message -> behavior.commentMessage(projectId, issueId, message)
                        .subscribeOn(Schedulers.boundedElastic())
                        .subscribe())
                .then();
    }

    public LocalDateTime parseApplyTime(final String command)
    {
        final var args = command.split(" ", 2);
        if(args.length == 1)
            return LocalDateTime.now();

        final var zonedDateTime = dateTimeFormatter.parse(args[1], ZonedDateTime::from);
        return zonedDateTime.toLocalDateTime();
    }

    @Override
    public String description()
    {
        return "HELP_COMMAND_APPLY_DESCRIPTION";
    }

    @Override
    public String options()
    {
        return "HELP_COMMAND_APPLY_OPTIONS";
    }

    @Override
    public Set<String> example()
    {
        return Set.of("HELP_COMMAND_APPLY_EXAMPLE_1");
    }
}
