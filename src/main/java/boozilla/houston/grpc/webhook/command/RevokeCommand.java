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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@AllArgsConstructor
public class RevokeCommand implements Command {
    private final MessageSourceAccessor messageSourceAccessor;
    private final DataRepository dataRepository;

    @Override
    public Set<String> aliases()
    {
        return Set.of("/revoke");
    }

    @Override
    public String commandTemplate()
    {
        return "/revoke";
    }

    @Override
    public Mono<Void> run(final String packageName, final long projectId, final long issueId,
                          final String targetRef, final String command, final GitBehavior<?> behavior)
    {
        final var excludeLabels = Stream.concat(Arrays.stream(StateLabel.values()).map(StateLabel::name), Stream.of(targetRef))
                .collect(Collectors.toUnmodifiableSet());

        return behavior.getIssue(projectId, issueId)
                .flatMapMany(issue -> Flux.fromIterable(issue.getLabels()))
                .filter(label -> !excludeLabels.contains(label))
                .flatMap(commitId -> dataRepository.updateByCommitIdIsLike(commitId + "%")
                        .map(affected -> Tuples.of(commitId, affected)))
                .collectList()
                .flatMap(affected -> {
                    final var revoked = new HashSet<String>();
                    final var notRevoked = new HashSet<String>();

                    affected.forEach(tuple -> {
                        if(tuple.getT2() > 0)
                            revoked.add(tuple.getT1());
                        else
                            notRevoked.add(tuple.getT1());
                    });

                    final var messages = new ArrayList<String>();

                    if(!revoked.isEmpty())
                    {
                        messages.add(messageSourceAccessor.getMessage("REVOKE_COMMAND_REVOKED_COMMIT")
                                .formatted(String.join(", ", revoked)));
                    }

                    if(!notRevoked.isEmpty())
                    {
                        messages.add(messageSourceAccessor.getMessage("REVOKE_COMMAND_AFFECTED_NOTHING")
                                .formatted(String.join(", ", notRevoked)));
                    }

                    return Mono.just(String.join("<br>", messages));
                })
                .doOnNext(affected -> behavior.commentMessage(projectId, issueId, affected)
                        .subscribeOn(Schedulers.boundedElastic())
                        .subscribe())
                .then();
    }

    @Override
    public String description()
    {
        return "HELP_COMMAND_REVOKE_DESCRIPTION";
    }

    @Override
    public String options()
    {
        return "HELP_COMMAND_REVOKE_OPTIONS";
    }

    @Override
    public Set<String> example()
    {
        return Set.of();
    }
}
