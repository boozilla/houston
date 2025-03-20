package boozilla.houston.grpc.webhook.client.github.repository;

import boozilla.houston.grpc.webhook.client.github.PaginationResponse;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public record RepositoryCompareResponse(
        List<Commit> commits,
        List<Diff> diffs
) implements PaginationResponse<RepositoryCompareResponse> {
    @JsonCreator
    public RepositoryCompareResponse(@JsonProperty("commits") List<Commit> commits,
                                     @JsonProperty("files") List<Diff> diffs)
    {
        this.commits = Objects.isNull(commits) ? List.of() : commits;
        this.diffs = Objects.isNull(diffs) ? List.of() : diffs;
    }

    @Override
    public RepositoryCompareResponse merge(final RepositoryCompareResponse other)
    {
        return new RepositoryCompareResponse(Stream.concat(commits.stream(), other.commits.stream()).toList(),
                Stream.concat(diffs.stream(), other.diffs.stream()).toList());
    }

    public record Commit(
            String sha,
            String message,
            String authorName
    ) {
        @JsonCreator
        public Commit(@JsonProperty("sha") String sha,
                      @JsonProperty("commit") Map<String, Object> commit,
                      @JsonProperty("author") Map<String, Object> author)
        {
            this(sha,
                    Objects.isNull(commit) ? null : (String) commit.get("message"),
                    Objects.isNull(author) ? null : (String) author.get("login"));
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
