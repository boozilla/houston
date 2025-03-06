package boozilla.houston.asset;

import boozilla.houston.asset.constraints.AssetSheetConstraints;
import boozilla.houston.repository.Vaults;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
public class Assets {
    private final List<AssetSheetConstraints> constraints;
    private final Set<Consumer<AssetContainer>> listeners;

    private AssetContainer container;

    private Assets(final List<AssetSheetConstraints> constraints, final Vaults vaults)
    {
        this.constraints = constraints;
        this.listeners = new HashSet<>();

        container(new AssetContainer(vaults));
    }

    public void onUpdated(final Consumer<AssetContainer> listener)
    {
        listeners.add(listener);

        listener.accept(container);
    }

    public AssetContainer container()
    {
        return this.container;
    }

    public void container(final AssetContainer container)
    {
        this.container = container;

        listeners.forEach(listener -> listener.accept(container));
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
