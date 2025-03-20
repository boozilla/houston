package boozilla.houston.grpc.webhook.client.github.issue;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public record IssueGetResponse(
        long number,
        Set<String> labels
) {
    @JsonCreator
    public IssueGetResponse(@JsonProperty("number") final long number,
                            @JsonProperty("labels") final List<Map<String, Object>> labels)
    {
        this(number, Objects.isNull(labels) ? Set.of() :
                labels.stream().map(l -> (String) l.get("name"))
                        .collect(Collectors.toUnmodifiableSet()));
    }
}
