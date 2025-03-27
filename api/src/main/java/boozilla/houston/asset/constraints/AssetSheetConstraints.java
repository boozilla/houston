package boozilla.houston.asset.constraints;

import reactor.core.publisher.Flux;

import java.io.PrintWriter;
import java.util.Optional;

public interface AssetSheetConstraints {
    Optional<String> targetSheetName();

    Flux<? extends Throwable> check(final PrintWriter writer,
                                    final AssetAccessor accessor);

    String subject();
}
