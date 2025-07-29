package boozilla.houston.grpc.webhook.client.gitlab;

import boozilla.houston.grpc.webhook.GitBehavior;
import boozilla.houston.grpc.webhook.StateLabel;
import boozilla.houston.grpc.webhook.client.Issue;
import boozilla.houston.grpc.webhook.client.gitlab.notes.NotesGetResponse;
import boozilla.houston.grpc.webhook.client.gitlab.repository.RepositoryCompareResponse;
import boozilla.houston.grpc.webhook.client.gitlab.repository.RepositoryTreeResponse;
import boozilla.houston.grpc.webhook.command.PayloadCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.linecorp.armeria.server.ServiceRequestContext;
import houston.vo.webhook.Contributor;
import houston.vo.webhook.UploadPayload;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.Period;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class GitLabBehavior implements GitBehavior<GitLabClient> {
    private final GitLabClient client;

    public GitLabBehavior(final ServiceRequestContext context,
                          final ObjectMapper objectMapper)
    {
        client = GitLabClient.of(context, objectMapper);
    }

    @Override
    public GitLabClient client()
    {
        return client;
    }

    @Override
    public Mono<UploadPayload> uploadPayload(final String projectId,
                                             final String assignee,
                                             final String ref,
                                             final String beforeCommitId,
                                             final String afterCommitId,
                                             final Set<String> additionalFiles)
    {
        return client.compare(projectId, beforeCommitId, afterCommitId)
                .map(compareResults -> {
                    final var title = compareResults.commit().title();
                    final var head = compareResults.commit().id();
                    final var contributors = Lists.reverse(compareResults.commits())
                            .stream()
                            .map(commit -> Contributor.newBuilder()
                                    .setName(commit.authorName())
                                    .setEmail(commit.authorEmail())
                                    .setTitle(commit.title())
                                    .setCommitId(commit.id())
                                    .build())
                            .toList();

                    final var excludeDeleted = compareResults.diffs()
                            .stream()
                            .filter(diff -> !diff.deletedFile())
                            .map(RepositoryCompareResponse.Diff::newPath)
                            .collect(Collectors.toUnmodifiableSet());
                    final var commitFiles = Stream.concat(
                                    additionalFiles.stream(),
                                    excludeDeleted.stream())
                            .collect(Collectors.toUnmodifiableSet());

                    return UploadPayload.newBuilder()
                            .setProjectId(projectId)
                            .setAssignee(assignee)
                            .setTitle(title)
                            .setRef(ref)
                            .setHead(head)
                            .addAllContributor(contributors)
                            .addAllCommitFile(commitFiles)
                            .build();
                });
    }

    public Mono<List<Issue>> openedIssues(final String projectId)
    {
        return client.findOpenedIssue(projectId)
                .cast(Issue.class)
                .collectList();
    }

    @Override
    public Mono<Issue> createIssue(final UploadPayload uploadPayload)
    {
        final var labels = String.join(",", branchFromRef(uploadPayload.getRef()), shortCommitId(uploadPayload.getHead()));

        return client.createIssue(
                uploadPayload.getProjectId(),
                uploadPayload.getTitle(),
                issueContents(uploadPayload),
                uploadPayload.getAssignee(),
                labels,
                ZonedDateTime.now());
    }

    @Override
    public Mono<Issue> getIssue(final String projectId, final String issueId)
    {
        return client.getIssue(projectId, issueId);
    }

    @Override
    public Mono<Void> linkIssues(final String projectId, final String issueIid, final List<Issue> linkedIssue)
    {
        return Flux.fromIterable(linkedIssue)
                .parallel()
                .filter(li -> !li.getId().equals(issueIid))
                .flatMap(li -> client.createIssueLink(projectId, issueIid, projectId, li.getId())
                        .then(client.closeIssue(projectId, li.getId())))
                .then();
    }

    @Override
    public Mono<Void> setState(final String projectId, final String issueId, final StateLabel state)
    {
        final var excludeLabels = Stream.of(StateLabel.values())
                .map(StateLabel::name)
                .collect(Collectors.toUnmodifiableSet());

        return client.getIssue(projectId, issueId)
                .flatMap(issue -> {
                    final var labels = Stream.concat(
                            issue.getLabels().stream().filter(label -> !excludeLabels.contains(label)),
                            Stream.of(state.name())
                    ).collect(Collectors.joining(","));

                    return client.updateIssueLabel(projectId, issueId, labels);
                });
    }

    @Override
    public Mono<Void> addLabels(final String projectId, final String issueId, final String... labels)
    {
        return client.getIssue(projectId, issueId)
                .flatMap(issue -> {
                    final var newLabels = Stream.concat(issue.getLabels().stream(), Arrays.stream(labels))
                            .collect(Collectors.toUnmodifiableSet());

                    return client.updateIssueLabel(projectId, issueId, String.join(",", newLabels));
                });
    }

    @Override
    public Mono<Void> commentMessage(final String projectId, final String issueIid, final String message)
    {
        return client.createIssueNote(projectId, issueIid, message)
                .doOnRequest(consumer -> log.info("{} [projectId={}, issueIid={}]", message, projectId, issueIid))
                .publishOn(Schedulers.newSingle("gitlab-comment-sender", true));
    }

    @Override
    public Mono<Void> commentExceptions(final String projectId, final String issueId, final Throwable exception)
    {
        return commentExceptions(projectId, issueId, List.of(exception));
    }

    @Override
    public Mono<Void> commentExceptions(final String projectId, final String issueId, final List<Throwable> exceptions)
    {
        return Flux.fromIterable(Lists.partition(exceptions, 10))
                .flatMap(partition -> Flux.fromIterable(partition)
                        .map(error -> ">%s".formatted(error.getMessage()))
                        .collectList()
                        .flatMap(messages -> commentMessage(projectId, issueId, String.join("<br>\n", messages))))
                .then(setState(projectId, issueId, StateLabel.ERROR));
    }

    @Override
    public Mono<Void> commentUploadPayload(final String issueIid, final UploadPayload uploadPayload)
    {
        return commentMessage(uploadPayload.getProjectId(), issueIid, payloadComment(uploadPayload));
    }

    @Override
    public Mono<byte[]> openFile(final String projectId, final String ref, final String path)
    {
        return client.getRawFile(projectId, ref, path, false);
    }

    @Override
    public Mono<String> findUploadPayload(final String projectId, final String issueIid)
    {
        final var alias = new PayloadCommand().aliases();

        return client.notes(projectId, issueIid)
                .filter(note -> alias.stream().anyMatch(a -> note.body().startsWith(a)))
                .map(NotesGetResponse.Note::body)
                .singleOrEmpty();
    }

    @Override
    public Mono<List<String>> allFiles(final String projectId, final String ref)
    {
        return client.tree(projectId, "/", ref, true)
                .filter(item -> item.type().contentEquals("blob"))
                .map(RepositoryTreeResponse.Node::path)
                .collectList();
    }

    @Override
    public Mono<String> commitId(final String projectId, final String ref)
    {
        return client.getBranches(projectId, ref)
                .map(branch -> branch.commit().id())
                .defaultIfEmpty(ref);
    }

    @Override
    public Mono<Void> addSpentTime(final String projectId, final String issueId, final Period period)
    {
        return client.addSpentTime(projectId, issueId, period);
    }

    @Override
    public Mono<Void> closeIssue(final String projectId, final String issueId)
    {
        return client.closeIssue(projectId, issueId);
    }
}
