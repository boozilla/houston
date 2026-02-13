package boozilla.houston.token.allowlist.source;

import boozilla.houston.properties.AdminTokenAllowlistProperties;
import boozilla.houston.token.allowlist.AllowlistParser;
import boozilla.houston.token.allowlist.AllowlistSnapshot;
import boozilla.houston.token.allowlist.AllowlistSource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.LinkedHashSet;
import java.util.Optional;

@Component
public class SpringConfigAllowlistSource implements AllowlistSource {
    private static final String INLINE_PROPERTY = "admin.token.allowlist.inline";

    private final AdminTokenAllowlistProperties.SpringConfig properties;
    private final AdminTokenAllowlistProperties allowlistProperties;
    private final Environment environment;

    public SpringConfigAllowlistSource(final AdminTokenAllowlistProperties allowlistProperties,
                                       final Environment environment)
    {
        this.allowlistProperties = allowlistProperties;
        this.properties = allowlistProperties.sources().springConfig();
        this.environment = environment;
    }

    @Override
    public String name()
    {
        return "spring-config";
    }

    @Override
    public boolean enabled()
    {
        return properties.enabled();
    }

    @Override
    public boolean isPollingSource()
    {
        return false;
    }

    @Override
    public long syncIntervalMs()
    {
        return -1;
    }

    @Override
    public void validateConfiguration()
    {
        // no-op
    }

    @Override
    public Mono<AllowlistSnapshot> loadInitial()
    {
        return Mono.fromCallable(() -> {
            final var inline = environment.getProperty(INLINE_PROPERTY, "");
            final var hashes = new LinkedHashSet<String>();
            hashes.addAll(AllowlistParser.parseAndHash(allowlistProperties.tokens()));
            hashes.addAll(AllowlistParser.parseAndHash(inline));

            return new AllowlistSnapshot(hashes, Integer.toString((allowlistProperties.tokens().toString() + inline).hashCode()));
        });
    }

    @Override
    public Mono<Optional<AllowlistSnapshot>> reloadIfChanged()
    {
        return Mono.just(Optional.empty());
    }
}
