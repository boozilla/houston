package boozilla.houston.token;

import boozilla.houston.security.KmsAlgorithm;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;

@Component
public class AdminApiKey {
    private final String issuer;
    private final KmsAlgorithm kmsAlgorithm;
    private final JWTVerifier verifier;

    public AdminApiKey(@Value("${project.name}") final String appName,
                       final KmsAlgorithm kmsAlgorithm)
    {
        this.issuer = appName;
        this.kmsAlgorithm = kmsAlgorithm;
        this.verifier = JWT.require(kmsAlgorithm)
                .withIssuer(issuer)
                .build();
    }

    public Mono<String> create(final String username)
    {
        return Mono.fromCallable(() -> JWT.create()
                        .withIssuedAt(OffsetDateTime.now().toInstant())
                        .withIssuer(issuer)
                        .withSubject(username)
                        .sign(kmsAlgorithm))
                .publishOn(Schedulers.boundedElastic());
    }

    public Mono<Boolean> verify(final Optional<String> token)
    {
        return Mono.justOrEmpty(token)
                .map(t -> Objects.nonNull(verifier.verify(t)))
                .defaultIfEmpty(false);
    }
}
