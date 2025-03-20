package boozilla.houston.grpc.webhook.client.gitlab.repository;

public class RepositoryTreeResponse {
    public record Node(
            String type,
            String path
    ) {

    }
}
