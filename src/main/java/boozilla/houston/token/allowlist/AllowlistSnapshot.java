package boozilla.houston.token.allowlist;

import java.util.Objects;
import java.util.Set;

public record AllowlistSnapshot(
        Set<String> tokens,
        String cursor
) {
    public AllowlistSnapshot
    {
        tokens = Set.copyOf(Objects.requireNonNullElse(tokens, Set.<String>of()));
    }
}
