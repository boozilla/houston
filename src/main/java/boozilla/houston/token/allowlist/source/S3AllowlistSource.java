package boozilla.houston.token.allowlist.source;

import boozilla.houston.properties.AdminTokenAllowlistProperties;
import boozilla.houston.token.allowlist.AllowlistParser;
import boozilla.houston.token.allowlist.AllowlistSnapshot;
import boozilla.houston.token.allowlist.AllowlistSource;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class S3AllowlistSource implements AllowlistSource {
    private final AdminTokenAllowlistProperties.S3 properties;
    private final S3AsyncClient client;
    private final AtomicReference<String> cursorRef = new AtomicReference<>();

    public S3AllowlistSource(final AdminTokenAllowlistProperties allowlistProperties)
    {
        this.properties = allowlistProperties.sources().s3();
        this.client = client();
    }

    @Override
    public String name()
    {
        return "s3";
    }

    @Override
    public boolean enabled()
    {
        return properties.enabled();
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
        if(Strings.isBlank(properties.bucket()) || Strings.isBlank(properties.key()))
        {
            throw new IllegalStateException("S3 allowlist source requires bucket and key");
        }

        if(Objects.isNull(properties.syncIntervalMs()) || properties.syncIntervalMs() <= 0)
        {
            throw new IllegalStateException("S3 allowlist source requires positive sync-interval-ms");
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
        final var future = client.getObject(builder -> builder
                        .bucket(properties.bucket())
                        .key(properties.key())
                        .build(),
                AsyncResponseTransformer.toBytes());

        return Mono.fromFuture(future)
                .map(responseBytes -> toSnapshot(responseBytes));
    }

    private AllowlistSnapshot toSnapshot(final ResponseBytes<GetObjectResponse> responseBytes)
    {
        final var payload = new String(responseBytes.asByteArray(), StandardCharsets.UTF_8);
        final var hashes = AllowlistParser.parseAndHash(payload);
        final var eTag = responseBytes.response().eTag();
        final var cursor = Objects.isNull(eTag) ? Integer.toString(payload.hashCode()) : eTag;

        return new AllowlistSnapshot(hashes, cursor);
    }

    private S3AsyncClient client()
    {
        if(Strings.isBlank(properties.region()))
        {
            return S3AsyncClient.builder()
                    .build();
        }

        return S3AsyncClient.builder()
                .region(Region.of(properties.region()))
                .build();
    }
}
