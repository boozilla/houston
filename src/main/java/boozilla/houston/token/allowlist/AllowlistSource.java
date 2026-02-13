package boozilla.houston.token.allowlist;

import reactor.core.publisher.Mono;

import java.util.Optional;

public interface AllowlistSource {
    String name();

    boolean enabled();

    boolean isPollingSource();

    long syncIntervalMs();

    void validateConfiguration();

    Mono<AllowlistSnapshot> loadInitial();

    Mono<Optional<AllowlistSnapshot>> reloadIfChanged();
}
