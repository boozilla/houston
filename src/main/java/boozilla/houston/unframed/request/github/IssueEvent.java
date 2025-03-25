package boozilla.houston.unframed.request.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IssueEvent(
        @JsonProperty("issue") Issue issue,
        @JsonProperty("repository") Repository repository,
        @JsonProperty("comment") Comment comment
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Issue(
            @JsonProperty("number") String number,
            @JsonProperty("labels") List<Label> labels
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Label(
                @JsonProperty("name") String name
        ) {
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Comment(
            @JsonProperty("body") String body
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Repository(
            @JsonProperty("full_name") String fullName,
            @JsonProperty("owner") User owner
    ) {
    }
}
