package boozilla.houston.token;

import boozilla.houston.security.KmsRsaAlgorithm;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.OffsetDateTime;
import java.util.Objects;

@Component
public class AdminApiKey {
    private final String issuer;
    private final KmsRsaAlgorithm kmsRsaAlgorithm;
    private final JWTVerifier verifier;

    public AdminApiKey(@Value("${project.name}") final String appName,
                       final KmsRsaAlgorithm kmsRsaAlgorithm)
    {
        this.issuer = appName;
        this.kmsRsaAlgorithm = kmsRsaAlgorithm;
        this.verifier = JWT.require(kmsRsaAlgorithm)
                .withIssuer(issuer)
                .build();
    }

    public Mono<String> create(final String username)
    {
        return Mono.fromCallable(() -> JWT.create()
                        .withIssuedAt(OffsetDateTime.now().toInstant())
                        .withIssuer(issuer)
                        .withSubject(username)
                        .sign(kmsRsaAlgorithm))
                .publishOn(Schedulers.boundedElastic());
    }

    public boolean verify(final String token)
    {
        return Objects.nonNull(verifier.verify(token));
    }
}
