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

    public static Set<String> parse(final String rawTokens)
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
            result.add(token);
        }

        return Set.copyOf(result);
    }

    public static Set<String> parse(final Iterable<String> rawTokens)
    {
        if(Objects.isNull(rawTokens))
        {
            return Set.of();
        }

        final var joined = new StringBuilder();

        for(final var raw : rawTokens)
        {
            final var token = Objects.requireNonNullElse(raw, "").trim();

            if(token.isBlank())
            {
                continue;
            }

            if(!joined.isEmpty())
            {
                joined.append('\n');
            }
            joined.append(token);
        }

        return parse(joined.toString());
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
