package boozilla.houston.grpc.webhook.client.gitlab.issue;

import boozilla.houston.grpc.webhook.client.Issue;

import java.util.Optional;
import java.util.Set;

public record IssueCreateResponse(
        String iid,
        Set<String> labels
) implements Issue {
    @Override
    public Optional<String> getUid()
    {
        return Optional.empty();
    }

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
