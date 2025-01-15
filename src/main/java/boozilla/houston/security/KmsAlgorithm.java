package boozilla.houston.security;

import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureGenerationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsAsyncClient;
import software.amazon.awssdk.services.kms.model.SigningAlgorithmSpec;

import java.util.Base64;
import java.util.Objects;

public class KmsAlgorithm extends Algorithm {
    private final KmsAsyncClient client;
    private final String keyId;
    private final SigningAlgorithmSpec signingAlgorithmSpec;

    public KmsAlgorithm(final String keyId, final KmsAsyncClient kmsAsyncClient)
    {
        super("KMS", "AWS KMS");

        this.client = kmsAsyncClient;
        this.keyId = keyId;

        final var describeKey = kmsAsyncClient.describeKey(builder -> builder.keyId(keyId));
        final var algorithms = Mono.fromFuture(describeKey)
                .map(response -> response.keyMetadata().signingAlgorithms())
                .block();

        signingAlgorithmSpec = Objects.requireNonNull(algorithms).getFirst();
    }

    @Override
    public void verify(final DecodedJWT decodedJWT) throws SignatureVerificationException
    {
        final var message = "%s.%s".formatted(decodedJWT.getHeader(), decodedJWT.getPayload());
        final var signature = Base64.getUrlDecoder().decode(decodedJWT.getSignature());
        final var verify = client.verify(builder -> builder.keyId(keyId)
                .message(SdkBytes.fromByteArray(message.getBytes()))
                .signingAlgorithm(signingAlgorithmSpec)
                .signature(SdkBytes.fromByteArray(signature)));

        final var response = Mono.fromFuture(verify)
                .block();

        if(!Objects.requireNonNull(response).signatureValid())
        {
            throw new SignatureVerificationException(this);
        }
    }

    @Override
    public byte[] sign(final byte[] bytes) throws SignatureGenerationException
    {
        final var sign = client.sign(builder -> builder.keyId(keyId)
                .signingAlgorithm(signingAlgorithmSpec)
                .message(SdkBytes.fromByteArray(bytes)));

        final var response = Mono.fromFuture(sign)
                .block();

        return Objects.requireNonNull(response)
                .signature()
                .asByteArray();
    }
}
