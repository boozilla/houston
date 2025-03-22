package boozilla.houston.repository.vaults;

import boozilla.houston.entity.Data;
import boozilla.houston.grpc.webhook.client.github.GitHubClient;
import reactor.core.publisher.Mono;

public class GitHubVaults implements Vaults {
    private final String repo;
    private final GitHubClient client;

    public GitHubVaults(final String repo, final GitHubClient client)
    {
        this.repo = repo;
        this.client = client;
    }

    @Override
    public Mono<UploadResult> upload(final String sheetName, final byte[] content)
    {
        return client.createBlob(repo, content);
    }

    @Override
    public Mono<byte[]> download(final Data data)
    {
        return client.getBlob(repo, data.getChecksum());
    }
}
