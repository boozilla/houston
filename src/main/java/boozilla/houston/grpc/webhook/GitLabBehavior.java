package boozilla.houston.grpc.webhook;

import boozilla.houston.grpc.webhook.client.Issue;
import boozilla.houston.grpc.webhook.client.gitlab.GitLabClient;
import boozilla.houston.grpc.webhook.client.gitlab.notes.NotesGetResponse;
import boozilla.houston.grpc.webhook.client.gitlab.repository.RepositoryCompareResponse;
import boozilla.houston.grpc.webhook.client.gitlab.repository.RepositoryTreeResponse;
import boozilla.houston.grpc.webhook.command.PayloadCommand;
import com.google.common.collect.Lists;
import com.linecorp.armeria.server.ServiceRequestContext;
import houston.vo.webhook.Contributor;
import houston.vo.webhook.UploadPayload;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.Duration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class GitLabBehavior implements GitBehavior<GitLabClient> {
    private final GitLabClient client;

    public GitLabBehavior(final String url, final ServiceRequestContext context)
    {
        client = GitLabClient.of(context);

        if(client.uri().getHost().compareTo(URI.create(url).getHost()) != 0)
            throw new IllegalArgumentException("GitLab URL is not matched");
    }

    @Override
    public GitLabClient client()
    {
        return client;
    }

    @Override
    public Mono<UploadPayload> uploadPayload(final String projectId, final long assignee,
                                             final String ref, final String beforeCommitId, final String afterCommitId)
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

                    final var commitFiles = compareResults.diffs()
                            .stream()
                            .filter(diff -> !diff.deletedFile())
                            .map(RepositoryCompareResponse.Diff::newPath)
                            .toList();

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
    public Mono<Void> linkIssues(final String issueIid, final UploadPayload uploadPayload)
    {
        final var projectId = uploadPayload.getProjectId();
        final var linkCommits = uploadPayload.getContributorList()
                .stream()
                .map(contributor -> shortCommitId(contributor.getCommitId()))
                .toList();

        return Flux.fromIterable(linkCommits)
                .parallel()
                .flatMap(label -> client.findIssue(projectId, List.of(label)))
                .filter(i -> !i.getIid().equals(issueIid))
                .flatMap(linkIssue -> client.createIssueLink(projectId, issueIid, projectId, linkIssue.getIid())
                        .then(client.closeIssue(projectId, linkIssue.getIid())))
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
        return client.getBranch(projectId, ref)
                .map(branch -> branch.commit().id())
                .defaultIfEmpty(ref);
    }

    @Override
    public Mono<Void> addSpentTime(final String projectId, final String issueId, final int seconds)
    {
        return client.addSpentTime(projectId, issueId, Duration.standardSeconds(seconds));
    }

    @Override
    public Mono<Void> closeIssue(final String projectId, final String issueId)
    {
        return client.closeIssue(projectId, issueId);
    }

    public Mono<byte[]> openFile(final String projectId, final String ref, final String path)
    {
        return client.getRawFile(projectId, path, ref, false);
    }

    /**
     * 브랜치 이름을 가져온다.
     *
     * @param ref 레퍼런스
     * @return 브랜치 이름
     */
    private String branchFromRef(final String ref)
    {
        return ref.substring(ref.lastIndexOf('/') + 1);
    }
}
