package boozilla.houston.decorator.auth;

import boozilla.houston.properties.GitHubProperties;
import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.server.ServiceRequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Slf4j
@Component
public class GitHubAuthorizer implements HttpAuthorizer {
    private static final String TOKEN_HEADER_NAME = "x-hub-signature-256";
    private static final String SIGNATURE_ALGORITHM = "HmacSHA256";

    private final GitHubProperties gitHubProperties;

    public GitHubAuthorizer(final GitHubProperties gitHubProperties)
    {
        this.gitHubProperties = gitHubProperties;
    }

    @Override
    public @Nonnull CompletionStage<Boolean> authorize(@Nonnull final ServiceRequestContext ctx,
                                                       @Nonnull final HttpRequest httpRequest)
    {
        final var secret = gitHubProperties.webhookSecret();
        final var header = Optional.ofNullable(httpRequest.headers().get(TOKEN_HEADER_NAME));

        if(header.isEmpty())
        {
            return CompletableFuture.completedFuture(false);
        }

        try
        {
            final var mac = Mac.getInstance(SIGNATURE_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(), SIGNATURE_ALGORITHM));

            return httpRequest.aggregate().thenApply(request -> {
                final var payload = request.contentUtf8();
                final var signatureBuilder = new StringBuilder("sha256=");

                for(final var b : mac.doFinal(payload.getBytes()))
                {
                    signatureBuilder.append(String.format("%02x", b));
                }

                final var expectedSignature = signatureBuilder.toString();

                return MessageDigest.isEqual(expectedSignature.getBytes(), header.get().getBytes());
            });
        }
        catch(Exception e)
        {
            log.error("Exception during HMAC validation", e);

            return CompletableFuture.completedFuture(false);
        }
    }
}
