package boozilla.houston.grpc.webhook;

import boozilla.houston.Application;
import boozilla.houston.context.GitContext;
import boozilla.houston.grpc.webhook.command.PayloadCommand;
import houston.vo.webhook.UploadPayload;
import org.gitlab4j.api.models.Issue;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public interface GitBehavior<T extends GitContext<?>> {
    T gitContext();

    Mono<UploadPayload> uploadPayload(final long projectId, final long assignee,
                                      final String ref, final String beforeCommitId, final String afterCommitId);

    Mono<Issue> createIssue(final UploadPayload uploadPayload);

    Mono<Issue> getIssue(final long projectId, final long issueId);

    Mono<Void> linkIssues(final long issueId, final UploadPayload uploadPayload);

    Mono<Void> setState(final long projectId, final long issueId, final StateLabel newLabel);

    Mono<Void> addLabels(final long projectId, final long issueId, final String... labels);

    Mono<Void> commentMessage(final long projectId, final long issueIid, final String message);

    Mono<Void> commentExceptions(final long projectId, final long issueId, final Throwable exception);

    Mono<Void> commentExceptions(final long projectId, final long issueId, final List<Throwable> exceptions);

    Mono<Void> commentUploadPayload(final long issueId, final UploadPayload uploadPayload);

    Mono<byte[]> openFile(final long projectId, final String ref, final String path);

    Mono<String> findUploadPayload(final long projectId, final long issueIid);

    Mono<List<String>> allFiles(final long projectId, final String ref);

    Mono<String> commitId(final long projectId, final String ref);

    Mono<Void> addSpentTime(final long projectId, final long issueId, final int seconds);

    Mono<Void> closeIssue(final long projectId, final long issueId);

    /**
     * 이슈 내용을 생성한다.
     *
     * @param uploadPayload 업로드 페이로드
     * @return 이슈 내용
     */
    default String issueContents(final UploadPayload uploadPayload)
    {
        final var template = """
                **ref**
                %s
                                
                **head**
                %s
                                
                %s
                ------
                %s
                                
                %s
                ------
                | %s | %s | %s | %s |
                | ------ | ------- | ------ | ------ |
                %s
                """;

        final var changeFiles = uploadPayload.getCommitFileList().stream()
                .map("- %s"::formatted)
                .collect(Collectors.joining("\n"));

        final var contributors = uploadPayload.getContributorList().stream()
                .map(contributor -> "| %s | %s | %s | %s |"
                        .formatted(contributor.getTitle(), shortCommitId(contributor.getCommitId()),
                                contributor.getName(), contributor.getEmail()))
                .collect(Collectors.joining("\n"));

        final var messageSourceAccessor = Application.messageSourceAccessor();

        return template.formatted(uploadPayload.getRef(),
                uploadPayload.getHead(),
                messageSourceAccessor.getMessage("ISSUE_CONTENTS_CHANGE_FILES"),
                changeFiles,
                messageSourceAccessor.getMessage("ISSUE_CONTENTS_CONTRIBUTORS"),
                messageSourceAccessor.getMessage("ISSUE_CONTENTS_TABLE_HEADER_TITLE"),
                messageSourceAccessor.getMessage("ISSUE_CONTENTS_TABLE_HEADER_COMMIT_ID"),
                messageSourceAccessor.getMessage("ISSUE_CONTENTS_TABLE_HEADER_NAME"),
                messageSourceAccessor.getMessage("ISSUE_CONTENTS_TABLE_HEADER_EMAIL"),
                contributors);
    }

    /**
     * Upload payload 코멘트 내용을 생성한다.
     *
     * @param uploadPayload 업로드 페이로드
     * @return 코멘트 내용
     */
    default String payloadComment(final UploadPayload uploadPayload)
    {
        final var template = new PayloadCommand().commandTemplate();
        final var encoded = Base64.getEncoder().encodeToString(uploadPayload.toByteArray());

        return template.formatted(encoded);
    }

    /**
     * 짧은 커밋 아이디로 변환
     *
     * @param commitId 커밋 아이디
     * @return 짧은 커밋 아이디
     */
    default String shortCommitId(final String commitId)
    {
        return commitId.substring(0, 8);
    }
}
