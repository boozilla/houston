package boozilla.houston.config;

import boozilla.houston.grpc.webhook.client.github.GitHubClient;
import boozilla.houston.repository.vaults.GitHubVaults;
import boozilla.houston.repository.vaults.S3Vaults;
import boozilla.houston.repository.vaults.Vaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VaultsConfig {
    @Bean
    @ConditionalOnProperty(prefix = "s3", name = "bucket")
    public Vaults s3Vaults(@Value("${s3.bucket}") final String bucket)
    {
        return new S3Vaults(bucket);
    }

    @Bean
    @ConditionalOnBean(GitHubClient.class)
    public Vaults githubVaults(@Value("${github.repo}") final String repo,
                               final GitHubClient client)
    {
        return new GitHubVaults(repo, client);
    }
}
