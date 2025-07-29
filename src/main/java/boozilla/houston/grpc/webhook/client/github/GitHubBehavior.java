package boozilla.houston.grpc.webhook.client.github;

import boozilla.houston.grpc.webhook.GitBehavior;
import boozilla.houston.grpc.webhook.StateLabel;
import boozilla.houston.grpc.webhook.client.Issue;
import boozilla.houston.grpc.webhook.client.github.issue.IssueGetCommentResponse;
import boozilla.houston.grpc.webhook.client.github.repository.RepositoryBranchesResponse;
import boozilla.houston.grpc.webhook.client.github.repository.RepositoryCompareResponse;
import boozilla.houston.grpc.webhook.client.github.repository.RepositoryTreesResponse;
import boozilla.houston.grpc.webhook.command.PayloadCommand;
import com.google.common.collect.Lists;
import houston.vo.webhook.Contributor;
import houston.vo.webhook.UploadPayload;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.Period;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@AllArgsConstructor
public class GitHubBehavior implements GitBehavior<GitHubClient> {
    private final GitHubClient client;

    @Override
    public GitHubClient client()
    {
        return client;
    }

    @Override
    public Mono<UploadPayload> uploadPayload(final String repo,
                                             final String assignee,
                                             final String ref,
                                             final String beforeCommitId,
                                             final String afterCommitId,
                                             final Set<String> additionalFiles)
    {
        return client.compare(repo, beforeCommitId, afterCommitId)
                .map(compareResults -> {
                    final var headCommit = compareResults.commits().getLast();
                    final var title = headCommit.message();
                    final var head = headCommit.sha();
                    final var contributors = Lists.reverse(compareResults.commits())
                            .stream()
                            .map(commit -> Contributor.newBuilder()
                                    .setName(commit.authorName())
                                    .setEmail(commit.authorEmail())
                                    .setTitle(commit.message())
                                    .setCommitId(commit.sha())
                                    .build())
                            .toList();

                    final var excludeDeleted = compareResults.diffs()
                            .stream()
                            .filter(diff -> !diff.deletedFile())
                            .map(RepositoryCompareResponse.Diff::newPath)
                            .toList();
                    final var commitFiles = Stream.concat(
                                    additionalFiles.stream(),
                                    excludeDeleted.stream())
                            .collect(Collectors.toUnmodifiableSet());

                    return UploadPayload.newBuilder()
                            .setProjectId(repo)
                            .setAssignee(assignee)
                            .setTitle(title)
                            .setRef(ref)
                            .setHead(head)
                            .addAllContributor(contributors)
                            .addAllCommitFile(commitFiles)
                            .build();
                });
    }

    public Mono<List<Issue>> openedIssues(final String repo)
    {
        return client.findOpenedIssue(repo)
                .cast(Issue.class)
                .collectList();
    }

    @Override
    public Mono<Issue> createIssue(final UploadPayload uploadPayload)
    {
        final var labels = Set.of(branchFromRef(uploadPayload.getRef()),
                shortCommitId(uploadPayload.getHead()));

        return client.createIssue(
                uploadPayload.getProjectId(),
                uploadPayload.getTitle(),
                issueContents(uploadPayload),
                uploadPayload.getAssignee(),
                labels);
    }

    @Override
    public Mono<Issue> getIssue(final String repo, final String issueId)
    {
        return client.getIssue(repo, issueId);
    }

    @Override
    public Mono<Void> linkIssues(final String repo, final String issueId, final List<Issue> linkedIssue)
    {
        return Flux.fromIterable(linkedIssue)
                .parallel()
                .filter(li -> !li.getId().equals(issueId))
                .flatMap(li -> client.createSubIssues(repo, issueId, li.getUid().orElseThrow())
                        .then(client.closeIssue(repo, li.getId())))
                .then();
    }

    @Override
    public Mono<Void> setState(final String repo, final String issueId, final StateLabel state)
    {
        final var excludeLabels = Stream.of(StateLabel.values())
                .map(StateLabel::name)
                .collect(Collectors.toUnmodifiableSet());

        return client.getIssue(repo, issueId)
                .flatMap(issue -> {
                    final var labels = Stream.concat(
                                    issue.getLabels().stream().filter(label -> !excludeLabels.contains(label)),
                                    Stream.of(state.name()))
                            .collect(Collectors.toUnmodifiableSet());

                    return client.updateIssueLabels(repo, issueId, labels);
                });
    }

    @Override
    public Mono<Void> addLabels(final String repo, final String issueId, final String... labels)
    {
        return client.getIssue(repo, issueId)
                .flatMap(issue -> {
                    final var newLabels = Stream.concat(issue.getLabels().stream(), Arrays.stream(labels))
                            .collect(Collectors.toUnmodifiableSet());

                    return client.updateIssueLabels(repo, issueId, newLabels);
                });
    }

    @Override
    public Mono<Void> commentMessage(final String repo, final String issueId, final String message)
    {
        return client.writeIssueComment(repo, issueId, message)
                .doOnRequest(consumer -> log.info("{} [projectId={}, issueNumber={}]", message, repo, issueId))
                .publishOn(Schedulers.newSingle("github-comment-sender", true));
    }

    @Override
    public Mono<Void> commentExceptions(final String repo, final String issueId, final Throwable exception)
    {
        return commentExceptions(repo, issueId, List.of(exception));
    }

    @Override
    public Mono<Void> commentExceptions(final String repo, final String issueId, final List<Throwable> exceptions)
    {
        return Flux.fromIterable(Lists.partition(exceptions, 10))
                .flatMap(partition -> Flux.fromIterable(partition)
                        .map(error -> ">%s".formatted(error.getMessage()))
                        .collectList()
                        .flatMap(messages -> commentMessage(repo, issueId, String.join("<br>\n", messages))))
                .then(setState(repo, issueId, StateLabel.ERROR));
    }

    @Override
    public Mono<Void> commentUploadPayload(final String issueId, final UploadPayload uploadPayload)
    {
        return commentMessage(uploadPayload.getProjectId(), issueId, payloadComment(uploadPayload));
    }

    @Override
    public Mono<byte[]> openFile(final String repo, final String ref, final String path)
    {
        return client.getRawFile(repo, ref, path);
    }

    @Override
    public Mono<String> findUploadPayload(final String repo, final String issueId)
    {
        final var alias = new PayloadCommand().aliases();

        return client.getIssueComments(repo, issueId)
                .filter(note -> alias.stream().anyMatch(a -> note.body().startsWith(a)))
                .map(IssueGetCommentResponse::body)
                .singleOrEmpty();
    }

    @Override
    public Mono<List<String>> allFiles(final String repo, final String ref)
    {
        return client.trees(repo, ref, true)
                .flatMapMany(item -> Flux.fromIterable(item.tree()))
                .filter(item -> item.type().contentEquals("blob"))
                .map(RepositoryTreesResponse.Node::path)
                .collectList();
    }

    @Override
    public Mono<String> commitId(final String repo, final String ref)
    {
        return client.branches(repo, ref)
                .map(RepositoryBranchesResponse::head)
                .defaultIfEmpty(ref);
    }

    @Override
    public Mono<Void> addSpentTime(final String repo, final String issueId, final Period period)
    {
        return Mono.empty();
    }

    @Override
    public Mono<Void> closeIssue(final String repo, final String issueId)
    {
        return client.closeIssue(repo, issueId);
    }
}
