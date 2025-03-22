package boozilla.houston.grpc.webhook.client.github.repository;

import boozilla.houston.grpc.webhook.client.github.CollectableResponse;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.logging.log4j.util.Strings;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public record RepositoryCompareResponse(
        List<Commit> commits,
        List<Diff> diffs
) implements CollectableResponse<RepositoryCompareResponse> {
    @JsonCreator
    public RepositoryCompareResponse(@JsonProperty("commits") List<Commit> commits,
                                     @JsonProperty("files") List<Diff> diffs)
    {
        this.commits = Objects.isNull(commits) ? List.of() : commits;
        this.diffs = Objects.isNull(diffs) ? List.of() : diffs;
    }

    @Override
    public RepositoryCompareResponse accumulate(final RepositoryCompareResponse other)
    {
        return new RepositoryCompareResponse(Stream.concat(commits.stream(), other.commits.stream()).toList(),
                Stream.concat(diffs.stream(), other.diffs.stream()).toList());
    }

    public record Commit(
            String sha,
            String message,
            String authorName,
            String authorEmail
    ) {
        @JsonCreator
        @SuppressWarnings("unchecked")
        public Commit(@JsonProperty("sha") String sha,
                      @JsonProperty("commit") Map<String, Object> commit)
        {
            this(sha,
                    (String) commit.getOrDefault("message", Strings.EMPTY),
                    (String) ((Map<String, Object>) commit.getOrDefault("author", Map.of())).getOrDefault("name", Strings.EMPTY),
                    (String) ((Map<String, Object>) commit.getOrDefault("author", Map.of())).getOrDefault("email", Strings.EMPTY));
        }
    }

    public record Diff(
            String newPath,
            boolean deletedFile
    ) {
        @JsonCreator
        public Diff(@JsonProperty("filename") String path,
                    @JsonProperty("status") String status)
        {
            this(path, status.contentEquals("removed"));
        }
    }
}
