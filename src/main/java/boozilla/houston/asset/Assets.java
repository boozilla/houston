package boozilla.houston.asset;

import boozilla.houston.asset.constraints.AssetSheetConstraints;
import boozilla.houston.repository.Vaults;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class Assets {
    private final List<AssetSheetConstraints> constraints;

    private AssetContainer container;

    private Assets(final List<AssetSheetConstraints> constraints, final Vaults vaults)
    {
        this.constraints = constraints;
        this.container = new AssetContainer(vaults);
    }

    public AssetContainer container()
    {
        return this.container;
    }

    public void container(final AssetContainer container)
    {
        this.container = container;
    }

    public AssetVerifier verifier(final AssetContainer container)
    {
        final var targetConstraints = container.updatedKey()
                .stream()
                .flatMap(key -> constraints.stream().filter(constraint -> {
                    final var targetSheetName = constraint.targetSheetName();
                    return targetSheetName.map(s -> s.equals(key.sheetName())).orElse(true);
                }))
                .collect(Collectors.toUnmodifiableSet());

        return new AssetVerifier(container, targetConstraints);
    }
}
