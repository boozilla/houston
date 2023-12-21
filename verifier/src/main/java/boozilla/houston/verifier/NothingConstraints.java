package boozilla.houston.verifier;

import boozilla.houston.asset.constraints.AssetAccessor;
import boozilla.houston.asset.constraints.AssetSheetConstraints;
import reactor.core.publisher.Flux;

import java.util.Optional;

public class NothingConstraints implements AssetSheetConstraints {
    @Override
    public Optional<String> targetSheetName()
    {
        return Optional.empty();
    }

    @Override
    public String subject()
    {
        return "Nothing";
    }

    @Override
    public Flux<Throwable> check(final AssetAccessor container)
    {
        return Flux.empty();
    }
}
