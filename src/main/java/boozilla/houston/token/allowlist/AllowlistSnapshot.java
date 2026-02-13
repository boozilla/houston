package boozilla.houston.token.allowlist;

import java.util.Objects;
import java.util.Set;

public record AllowlistSnapshot(
        Set<String> tokenHashes,
        String cursor
) {
    public AllowlistSnapshot
    {
        tokenHashes = Set.copyOf(Objects.requireNonNullElse(tokenHashes, Set.<String>of()));
    }
}
