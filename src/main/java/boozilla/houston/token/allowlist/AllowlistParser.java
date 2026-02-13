package boozilla.houston.token.allowlist;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public final class AllowlistParser {
    private AllowlistParser()
    {
    }

    public static Set<String> parseAndHash(final String rawTokens)
    {
        if(Objects.isNull(rawTokens) || rawTokens.isBlank())
        {
            return Set.of();
        }

        final var result = new LinkedHashSet<String>();
        final var candidates = rawTokens.split("[,\\r\\n]+");

        for(int i = 0; i < candidates.length; i++)
        {
            final var token = candidates[i].trim();

            if(token.isBlank())
            {
                continue;
            }

            validateJwt(token, i);
            result.add(TokenHashing.sha256(token));
        }

        return Set.copyOf(result);
    }

    public static Set<String> parseAndHash(final Iterable<String> rawTokens)
    {
        if(Objects.isNull(rawTokens))
        {
            return Set.of();
        }

        final var result = new LinkedHashSet<String>();
        int index = 0;

        for(final var raw : rawTokens)
        {
            final var token = Objects.requireNonNullElse(raw, "").trim();

            if(token.isBlank())
            {
                index++;
                continue;
            }

            validateJwt(token, index);
            result.add(TokenHashing.sha256(token));
            index++;
        }

        return Set.copyOf(result);
    }

    private static void validateJwt(final String token, final int index)
    {
        try
        {
            JWT.decode(token);
        }
        catch(final JWTDecodeException e)
        {
            throw new IllegalStateException("Invalid JWT in admin allowlist [index=%d]".formatted(index), e);
        }
    }
}
