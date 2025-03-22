package boozilla.houston.unframed.request.gitlab;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NoteEvent(
        @JsonProperty("project") Project project,
        @JsonProperty("object_attributes") ObjectAttributes objectAttributes,
        @JsonProperty("issue") Issue issue
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Project(
            @JsonProperty("id") String id
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ObjectAttributes(
            @JsonProperty("note") String note
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Issue(
            @JsonProperty("iid") String iid,
            @JsonProperty("labels") List<Label> labels
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Label(
                @JsonProperty("title") String title
        ) {
        }
    }
}
