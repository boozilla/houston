package boozilla.houston.grpc.webhook.client.github.issue;

import java.util.Set;

public record IssueCreateRequest(
        String title,
        String body,
        String assignee,
        Set<String> labels
) {

}
