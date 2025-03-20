package boozilla.houston.grpc.webhook.client.github.repository;

import java.util.List;

public record RepositoryTreesResponse(
        List<Node> tree,
        boolean truncated
) {
    public record Node(
            String path,
            String type
    ) {
    }
}
