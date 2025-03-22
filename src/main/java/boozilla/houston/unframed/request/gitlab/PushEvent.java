package boozilla.houston.unframed.request.gitlab;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PushEvent(
        @JsonProperty("before") String before,
        @JsonProperty("after") String after,
        @JsonProperty("ref") String ref,
        @JsonProperty("user_id") long userId,
        @JsonProperty("project_id") String projectId
) {

}
