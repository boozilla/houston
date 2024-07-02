package boozilla.houston.grpc.webhook;

import boozilla.houston.context.GitLabContext;
import boozilla.houston.grpc.webhook.command.PayloadCommand;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import houston.vo.webhook.Contributor;
import houston.vo.webhook.UploadPayload;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.Constants;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.IssuesApi;
import org.gitlab4j.api.models.*;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class GitLabBehavior implements GitBehavior<GitLabContext> {
    private final GitLabContext gitContext;

    public GitLabBehavior(final GitLabContext gitContext)
    {
        this.gitContext = gitContext;
    }

    @Override
    public GitLabContext gitContext()
    {
        return this.gitContext;
    }

    @Override
    public Mono<UploadPayload> uploadPayload(final long projectId, final long assignee,
                                             final String ref, final String beforeCommitId, final String afterCommitId)
    {
        return gitContext().api(client -> {
            final var repositoryApi = client.getRepositoryApi();

            try
            {
                final var compareResults = repositoryApi.compare(projectId, beforeCommitId, afterCommitId);
                final var title = compareResults.getCommit().getTitle();
                final var head = compareResults.getCommit().getId();
                final var contributors = Lists.reverse(compareResults.getCommits())
                        .stream()
                        .map(commit -> Contributor.newBuilder()
                                .setName(commit.getAuthorName())
                                .setEmail(commit.getAuthorEmail())
                                .setTitle(commit.getTitle())
                                .setCommitId(commit.getId())
                                .build())
                        .toList();
                final var commitFiles = compareResults.getDiffs()
                        .stream()
                        .filter(diff -> !diff.getDeletedFile())
                        .map(Diff::getNewPath)
                        .toList();

                final var uploadPayload = UploadPayload.newBuilder()
                        .setProjectId(projectId)
                        .setAssignee(assignee)
                        .setTitle(title)
                        .setRef(ref)
                        .setHead(head)
                        .addAllContributor(contributors)
                        .addAllCommitFile(commitFiles)
                        .build();

                return Mono.just(uploadPayload);
            }
            catch(GitLabApiException e)
            {
                return Mono.error(e);
            }
        });
    }

    @Override
    public Mono<Issue> createIssue(final UploadPayload uploadPayload)
    {
        return gitContext().api(client -> {
            final var issuesApi = client.getIssuesApi();
            final var labels = String.join(",", branchFromRef(uploadPayload.getRef()), shortCommitId(uploadPayload.getHead()));

            try
            {
                final var issue = issuesApi.createIssue(uploadPayload.getProjectId(),
                        uploadPayload.getTitle(), issueContents(uploadPayload),
                        null, List.of(uploadPayload.getAssignee()), null, labels, new Date(),
                        null, null, null, null);

                return Mono.just(issue);
            }
            catch(GitLabApiException e)
            {
                return Mono.error(e);
            }
        });
    }

    @Override
    public Mono<Issue> getIssue(final long projectId, final long issueId)
    {
        return gitContext().api(client -> {
            final var issuesApi = client.getIssuesApi();

            try
            {
                final var issue = issuesApi.getIssue(projectId, issueId);
                return Mono.just(issue);
            }
            catch(GitLabApiException e)
            {
                return Mono.error(e);
            }
        });
    }

    @Override
    public Mono<Void> linkIssues(final long issueIid, final UploadPayload uploadPayload)
    {
        return gitContext().api(client -> {
            final var issuesApi = client.getIssuesApi();
            final var projectId = uploadPayload.getProjectId();
            final var linkCommits = uploadPayload.getContributorList()
                    .stream()
                    .map(contributor -> shortCommitId(contributor.getCommitId()))
                    .toList();

            return Flux.fromIterable(linkCommits)
                    .parallel()
                    .flatMap(label -> Flux.fromStream(findIssueByLabels(issuesApi, projectId, List.of(label))))
                    .filter(i -> !i.getIid().equals(issueIid))
                    .doOnNext(linkIssue -> {
                        try
                        {
                            issuesApi.createIssueLink(projectId, issueIid, projectId, linkIssue.getIid(), LinkType.RELATES_TO);
                            issuesApi.closeIssue(projectId, linkIssue.getIid());
                        }
                        catch(GitLabApiException e)
                        {
                            throw new RuntimeException("Error linking to an issue", e);
                        }
                    })
                    .then();
        });
    }

    @Override
    public Mono<Void> setState(final long projectId, final long issueId, final StateLabel state)
    {
        return gitContext().api(client -> {
            final var issueApi = client.getIssuesApi();

            try
            {
                final var excludeLabels = Stream.of(StateLabel.values())
                        .map(StateLabel::name)
                        .collect(Collectors.toUnmodifiableSet());
                final var issue = issueApi.getIssue(projectId, issueId);
                final var labels = Streams.concat(issue.getLabels().stream()
                                        .filter(label -> !excludeLabels.contains(label)),
                                Stream.of(state.name()))
                        .collect(Collectors.joining(","));

                issueApi.updateIssue(projectId, issueId,
                        null, null, null, null, null,
                        labels,
                        null, new Date(), null);

                return Mono.empty();
            }
            catch(GitLabApiException e)
            {
                return Mono.error(e);
            }
        });
    }

    @Override
    public Mono<Void> addLabels(final long projectId, final long issueId, final String... labels)
    {
        return gitContext().api(client -> {
            final var issueApi = client.getIssuesApi();

            try
            {
                final var issue = issueApi.getIssue(projectId, issueId);
                final var newLabels = Stream.concat(issue.getLabels().stream(), Arrays.stream(labels))
                        .collect(Collectors.toUnmodifiableSet());

                issueApi.updateIssue(projectId, issueId,
                        null, null, null, null, null,
                        String.join(",", newLabels),
                        null, new Date(), null);

                return Mono.empty();
            }
            catch(GitLabApiException e)
            {
                return Mono.error(e);
            }
        });
    }

    @Override
    public Mono<Void> commentMessage(final long projectId, final long issueIid, final String message)
    {
        return gitContext().api(client -> {
                    final var notesApi = client.getNotesApi();

                    try
                    {
                        notesApi.createIssueNote(projectId, issueIid, message);

                        return Mono.empty();
                    }
                    catch(GitLabApiException e)
                    {
                        return Mono.error(e);
                    }
                    finally
                    {
                        log.info("{} [projectId={}, issueIid={}]", message, projectId, issueIid);
                    }
                })
                .publishOn(Schedulers.newSingle("gitlab-comment-sender", true))
                .then();
    }

    @Override
    public Mono<Void> commentExceptions(final long projectId, final long issueId, final Throwable exception)
    {
        return commentExceptions(projectId, issueId, List.of(exception));
    }

    @Override
    public Mono<Void> commentExceptions(final long projectId, final long issueId, final List<Throwable> exceptions)
    {
        return Flux.fromIterable(Lists.partition(exceptions, 10))
                .flatMap(partition -> Flux.fromIterable(partition)
                        .map(error -> ">%s".formatted(error.getMessage()))
                        .collectList()
                        .flatMap(messages -> commentMessage(projectId, issueId, String.join("<br>\n", messages))))
                .then(setState(projectId, issueId, StateLabel.ERROR));
    }

    @Override
    public Mono<Void> commentUploadPayload(final long issueIid, final UploadPayload uploadPayload)
    {
        return commentMessage(uploadPayload.getProjectId(), issueIid, payloadComment(uploadPayload));
    }

    @Override
    public Mono<String> findUploadPayload(final long projectId, final long issueIid)
    {
        return gitContext().api(client -> {
            final var notesApi = client.getNotesApi();

            try
            {
                final var alias = new PayloadCommand().aliases();
                final var optNote = notesApi.getIssueNotesStream(projectId, issueIid)
                        .filter(note -> alias.stream().anyMatch(a -> note.getBody().startsWith(a)))
                        .findAny();

                return Mono.justOrEmpty(optNote)
                        .map(Note::getBody);
            }
            catch(GitLabApiException e)
            {
                return Mono.error(e);
            }
        });
    }

    @Override
    public Mono<List<String>> allFiles(final long projectId, final String ref)
    {
        return gitContext().api(client -> {
            final var repositoryApi = client.getRepositoryApi();

            try
            {
                return Flux.fromStream(repositoryApi.getTreeStream(projectId, "/", ref, true))
                        .filter(treeItem -> treeItem.getType() == TreeItem.Type.BLOB)
                        .map(TreeItem::getPath)
                        .collectList();
            }
            catch(GitLabApiException e)
            {
                return Mono.error(e);
            }
        });
    }

    @Override
    public Mono<String> commitId(final long projectId, final String ref)
    {
        return gitContext().api(client -> {
            try
            {
                final var repositoryApi = client.getRepositoryApi();
                final var branch = repositoryApi.getOptionalBranch(projectId, ref);

                return branch.map(value -> Mono.just(value.getCommit().getId()))
                        .orElseGet(() -> Mono.just(ref));
            }
            catch(GitLabApiException e)
            {
                return Mono.error(e);
            }
        });
    }

    @Override
    public Mono<Void> addSpentTime(final long projectId, final long issueId, final int seconds)
    {
        if(seconds == 0)
            return Mono.empty();

        return gitContext().api(client -> {
            final var issuesApi = client.getIssuesApi();

            try
            {
                issuesApi.addSpentTime(projectId, issueId, seconds);

                return Mono.empty();
            }
            catch(GitLabApiException e)
            {
                return Mono.error(e);
            }
        });
    }

    @Override
    public Mono<Void> closeIssue(final long projectId, final long issueId)
    {
        return gitContext().api(client -> {
            final var issuesApi = client.getIssuesApi();

            try
            {
                issuesApi.closeIssue(projectId, issueId);

                return Mono.empty();
            }
            catch(GitLabApiException e)
            {
                return Mono.error(e);
            }
        });
    }

    public Mono<byte[]> openFile(final long projectId, final String ref, final String path)
    {
        return gitContext().api(client -> {
                    try
                    {
                        final var in = client.getRepositoryFileApi().getRawFile(projectId, ref, path);

                        return DataBufferUtils.readInputStream(() -> in,
                                        DefaultDataBufferFactory.sharedInstance,
                                        DefaultDataBufferFactory.DEFAULT_INITIAL_CAPACITY)
                                .reduce(new ByteArrayOutputStream(), (outputStream, dataBuffer) -> {
                                    final var bytes = new byte[dataBuffer.readableByteCount()];
                                    dataBuffer.read(bytes);
                                    outputStream.writeBytes(bytes);

                                    return outputStream;
                                })
                                .map(ByteArrayOutputStream::toByteArray);
                    }
                    catch(GitLabApiException e)
                    {
                        return Mono.error(e);
                    }
                });
    }

    /**
     * 이슈를 커밋 아이디로 찾는다.
     *
     * @param issuesApi 이슈 API
     * @param projectId 프로젝트 아이디
     * @param labels    레이블
     * @return 이슈 스트림
     */
    private Stream<Issue> findIssueByLabels(final IssuesApi issuesApi, final long projectId, final List<String> labels)
    {
        final var filter = new IssueFilter()
                .withScope(Constants.IssueScope.ALL)
                .withLabels(labels);

        try
        {
            return issuesApi.getIssuesStream(projectId, filter);
        }
        catch(GitLabApiException e)
        {
            throw new RuntimeException("Error finding issue by labels [projectId=%d, labels=%s]"
                    .formatted(projectId, labels), e);
        }
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
