package boozilla.houston.grpc.webhook.client.gitlab.issue;

import boozilla.houston.grpc.webhook.client.Issue;

import java.util.Set;

public record IssueCreateResponse(
        String iid,
        Set<String> labels
) implements Issue {
    @Override
    public String getId()
    {
        return iid();
    }

    @Override
    public Set<String> getLabels()
    {
        return labels();
    }
}
