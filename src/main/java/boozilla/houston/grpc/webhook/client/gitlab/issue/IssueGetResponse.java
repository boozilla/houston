package boozilla.houston.grpc.webhook.client.gitlab.issue;

import boozilla.houston.grpc.webhook.client.Issue;

import java.util.List;
import java.util.Optional;

public record IssueGetResponse(
        String iid,
        List<String> labels
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
    public List<String> getLabels()
    {
        return labels();
    }
}
