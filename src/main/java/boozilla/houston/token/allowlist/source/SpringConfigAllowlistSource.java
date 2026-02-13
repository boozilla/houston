package boozilla.houston.token.allowlist.source;

import boozilla.houston.properties.AdminTokenAllowlistProperties;
import boozilla.houston.token.allowlist.AllowlistParser;
import boozilla.houston.token.allowlist.AllowlistSnapshot;
import boozilla.houston.token.allowlist.AllowlistSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
@ConditionalOnProperty(prefix = "admin.token.allowlist.sources.spring-config", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SpringConfigAllowlistSource implements AllowlistSource {
    private final AdminTokenAllowlistProperties allowlistProperties;

    public SpringConfigAllowlistSource(final AdminTokenAllowlistProperties allowlistProperties)
    {
        this.allowlistProperties = allowlistProperties;
    }

    @Override
    public String name()
    {
        return "spring-config";
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
        return Mono.fromCallable(() -> new AllowlistSnapshot(
                AllowlistParser.parse(allowlistProperties.tokens()),
                Integer.toString(allowlistProperties.tokens().toString().hashCode())));
    }

    @Override
    public Mono<Optional<AllowlistSnapshot>> reloadIfChanged()
    {
        return Mono.just(Optional.empty());
    }
}
