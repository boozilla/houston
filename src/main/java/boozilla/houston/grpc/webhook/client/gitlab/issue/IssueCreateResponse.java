package boozilla.houston.grpc.webhook.client.gitlab.issue;

import boozilla.houston.grpc.webhook.client.Issue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record IssueCreateResponse(
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
