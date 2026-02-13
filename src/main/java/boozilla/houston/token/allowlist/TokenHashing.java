package boozilla.houston.token.allowlist;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class TokenHashing {
    private TokenHashing()
    {
    }

    public static String sha256(final String token)
    {
        try
        {
            final var digest = MessageDigest.getInstance("SHA-256");
            final var hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        }
        catch(final NoSuchAlgorithmException e)
        {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }
}
