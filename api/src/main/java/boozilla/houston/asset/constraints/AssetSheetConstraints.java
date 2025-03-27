package boozilla.houston.asset.constraints;

import reactor.core.publisher.Flux;

import java.util.Optional;

public interface AssetSheetConstraints {
    Optional<String> targetSheetName();

    Flux<? extends Throwable> check(final AssetAccessor accessor);

    String subject();
}
