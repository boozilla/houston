package boozilla.houston.grpc.webhook.client.github.repository;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;

public record RepositoryBranchesResponse(
        String head
) {
    public RepositoryBranchesResponse(@JsonProperty("commit") final Map<String, Object> commit)
    {
        this(Objects.isNull(commit) ? null : (String) commit.get("sha"));
    }
}
