package boozilla.houston.grpc.webhook.client.gitlab.repository;

import java.util.List;

public record RepositoryCompareResponse(
        Commit commit,
        List<Commit> commits,
        List<Diff> diffs
) {
    public record Commit(
            String id,
            String title,
            String authorName,
            String authorEmail
    ) {
    }

    public record Diff(
            String newPath,
            boolean deletedFile
    ) {
    }
}
