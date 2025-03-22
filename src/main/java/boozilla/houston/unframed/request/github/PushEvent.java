package boozilla.houston.unframed.request.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PushEvent(
        @JsonProperty("ref") String ref,
        @JsonProperty("before") String before,
        @JsonProperty("after") String after,
        @JsonProperty("repository") Repository repository,
        @JsonProperty("sender") User sender
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Repository(
            @JsonProperty("full_name") String fullName,
            @JsonProperty("owner") User owner
    ) {
    }
}
