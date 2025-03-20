package boozilla.houston.grpc.webhook.client.gitlab.repository;

public class RepositoryBranchResponse {
    public record Branch(
            Commit commit
    ) {
        public record Commit(
                String id
        ) {
        }
    }
}
