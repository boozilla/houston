package boozilla.houston.unframed.request.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IssueEvent(
        @JsonProperty("issue") Issue issue,
        @JsonProperty("repository") Repository repository
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Issue(
            @JsonProperty("number") String number,
            @JsonProperty("labels") List<Label> labels,
            @JsonProperty("body") String body
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Label(
                @JsonProperty("name") String name
        ) {
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Repository(
            @JsonProperty("full_name") String fullName,
            @JsonProperty("owner") User owner
    ) {
    }
}
