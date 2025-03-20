package boozilla.houston.grpc.webhook.client.gitlab.issue;

import boozilla.houston.grpc.webhook.client.Issue;

import java.util.List;

public record IssueGetResponse(
        String iid,
        List<String> labels
) implements Issue {
    @Override
    public String getIid()
    {
        return iid();
    }

    @Override
    public List<String> getLabels()
    {
        return labels();
    }
}
