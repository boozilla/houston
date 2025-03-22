package boozilla.houston.grpc.webhook.client.github.issue;

import boozilla.houston.grpc.webhook.client.Issue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public record IssueGetResponse(
        long uid,
        String number,
        Set<String> labels
) implements Issue {
    @JsonCreator
    public IssueGetResponse(@JsonProperty("id") final long uid,
                            @JsonProperty("number") final String number,
                            @JsonProperty("labels") final List<Map<String, Object>> labels)
    {
        this(uid, number,
                Objects.isNull(labels) ? Set.of() : labels.stream().map(l -> (String) l.get("name"))
                        .collect(Collectors.toUnmodifiableSet()));
    }

    @Override
    public String getId()
    {
        return number;
    }

    @Override
    public Set<String> getLabels()
    {
        return labels;
    }
}
