package boozilla.houston.repository.vaults;

import boozilla.houston.entity.Data;
import boozilla.houston.grpc.webhook.client.github.GitHubClient;
import reactor.core.publisher.Mono;

import java.util.Objects;

public class GitHubVaults implements Vaults {
    private final String owner;
    private final String repo;
    private final GitHubClient client;

    public GitHubVaults(final String repo, final GitHubClient client)
    {
        final var split = repo.split("/", 2);
        this.owner = Objects.requireNonNull(split[0], "Repository owner is required");
        this.repo = Objects.requireNonNull(split[1], "Repository name is required");
        this.client = client;
    }

    @Override
    public Mono<UploadResult> upload(final String sheetName, final byte[] content)
    {
        return client.createBlob(owner, repo, content);
    }

    @Override
    public Mono<byte[]> download(final Data data)
    {
        return client.getBlob(owner, repo, data.getChecksum());
    }
}
