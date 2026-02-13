package boozilla.houston.token.allowlist.source;

import boozilla.houston.properties.AdminTokenAllowlistProperties;
import boozilla.houston.token.allowlist.AllowlistParser;
import boozilla.houston.token.allowlist.AllowlistSnapshot;
import boozilla.houston.token.allowlist.AllowlistSource;
import org.apache.logging.log4j.util.Strings;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClient;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Component
@ConditionalOnProperty(prefix = "admin.token.allowlist.sources.secrets-manager", name = "enabled", havingValue = "true")
public class SecretsManagerAllowlistSource implements AllowlistSource {
    private static final String VERSION_STAGE = "AWSCURRENT";

    private final AdminTokenAllowlistProperties.SecretsManager properties;
    private final SecretsManagerAsyncClient client;
    private final AtomicReference<String> cursorRef = new AtomicReference<>();

    public SecretsManagerAllowlistSource(final AdminTokenAllowlistProperties allowlistProperties)
    {
        this.properties = allowlistProperties.sources()
                .secretsManager();
        this.client = client();
    }

    @Override
    public String name()
    {
        return "secrets-manager";
    }

    @Override
    public boolean isPollingSource()
    {
        return true;
    }

    @Override
    public long syncIntervalMs()
    {
        return Optional.ofNullable(properties.syncIntervalMs())
                .orElse(-1L);
    }

    @Override
    public void validateConfiguration()
    {
        if(Strings.isBlank(properties.secretId()))
        {
            throw new IllegalStateException("SecretsManager allowlist source requires secret-id");
        }

        if(Objects.isNull(properties.syncIntervalMs()) || properties.syncIntervalMs() <= 0)
        {
            throw new IllegalStateException("SecretsManager allowlist source requires positive sync-interval-ms");
        }
    }

    @Override
    public Mono<AllowlistSnapshot> loadInitial()
    {
        return fetch().doOnNext(snapshot -> cursorRef.set(snapshot.cursor()));
    }

    @Override
    public Mono<Optional<AllowlistSnapshot>> reloadIfChanged()
    {
        return fetch().map(snapshot -> {
            final var previous = cursorRef.get();

            if(Objects.equals(previous, snapshot.cursor()))
            {
                return Optional.<AllowlistSnapshot>empty();
            }

            cursorRef.set(snapshot.cursor());
            return Optional.of(snapshot);
        });
    }

    private Mono<AllowlistSnapshot> fetch()
    {
        final var request = software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest.builder()
                .secretId(properties.secretId())
                .versionStage(VERSION_STAGE)
                .build();

        return Mono.fromFuture(client.getSecretValue(request))
                .map(response -> {
                    final var raw = rawValue(response.secretString(), response.secretBinary() != null ?
                            response.secretBinary().asByteArray() : null);
                    final var tokens = AllowlistParser.parse(raw);
                    final var cursor = response.versionId();

                    return new AllowlistSnapshot(tokens, cursor);
                });
    }

    private String rawValue(final String secretString, final byte[] secretBinary)
    {
        if(Objects.nonNull(secretString))
        {
            return secretString;
        }

        if(Objects.nonNull(secretBinary))
        {
            return new String(secretBinary, StandardCharsets.UTF_8);
        }

        return "";
    }

    private SecretsManagerAsyncClient client()
    {
        if(Strings.isBlank(properties.region()))
        {
            return SecretsManagerAsyncClient.builder()
                    .build();
        }

        return SecretsManagerAsyncClient.builder()
                .region(Region.of(properties.region()))
                .build();
    }
}
