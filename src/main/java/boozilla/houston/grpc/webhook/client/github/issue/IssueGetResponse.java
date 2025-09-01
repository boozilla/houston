package boozilla.houston.grpc.webhook.client.github.issue;

import boozilla.houston.grpc.webhook.client.Issue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;
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
    @JsonProperty("id")
    public Optional<String> getUid()
    {
        return Optional.of(Long.toString(uid));
    }

    @Override
    @JsonProperty("number")
    public String getId()
    {
        return number;
    }

    @Override
    @JsonProperty("labels")
    public Set<String> getLabels()
    {
        return labels;
    }
}
