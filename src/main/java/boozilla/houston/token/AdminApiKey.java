package boozilla.houston.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AdminApiKey {
    private final String issuer;
    private final Map<String, Algorithm> algorithms;
    private final Function<Algorithm, JWTVerifier> verifier;

    public AdminApiKey(@Value("${project.name}") final String appName,
                       final Collection<Algorithm> algorithms)
    {
        this.issuer = appName;
        this.algorithms = algorithms.stream()
                .collect(Collectors.toUnmodifiableMap(Algorithm::getName, Function.identity()));
        this.verifier = algorithm -> JWT.require(algorithm)
                .withIssuer(issuer)
                .build();
    }

    public Mono<String> create(final String username, final Algorithm algorithm)
    {
        return Mono.fromCallable(() -> JWT.create()
                        .withIssuedAt(OffsetDateTime.now().toInstant())
                        .withIssuer(issuer)
                        .withSubject(username)
                        .sign(algorithm))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Boolean> verify(final Optional<String> token)
    {
        return Mono.justOrEmpty(token)
                .map(t -> {
                    final var decoded = JWT.decode(t);
                    final var algorithm = algorithms.get(decoded.getAlgorithm());

                    if(Objects.isNull(algorithm))
                    {
                        log.error("Unsupported algorithm [name={}]", decoded.getAlgorithm());

                        return false;
                    }

                    try
                    {
                        verifier.apply(algorithm)
                                .verify(decoded);

                        return true;
                    }
                    catch(final JWTVerificationException e)
                    {
                        log.info("Failed to verify admin API token", e);

                        return false;
                    }
                })
                .defaultIfEmpty(false);
    }
}
