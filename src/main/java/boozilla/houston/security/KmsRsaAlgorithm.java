package boozilla.houston.security;

import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureGenerationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsAsyncClient;
import software.amazon.awssdk.services.kms.model.SigningAlgorithmSpec;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Objects;

public class KmsRsaAlgorithm extends Algorithm {
    private final KmsAsyncClient kmsAsyncClient;
    private final Algorithm verifyAlgorithm;
    private final String keyId;

    // Note: KMS key rotation is not supported
    public KmsRsaAlgorithm(final String keyId, final KmsAsyncClient kmsAsyncClient)
    {
        super("AWS-KMS-RSA256", "ID: " + keyId);

        this.keyId = keyId;
        this.kmsAsyncClient = kmsAsyncClient;

        final var getPublicKey = kmsAsyncClient.getPublicKey(builder -> builder.keyId(keyId));

        this.verifyAlgorithm = Mono.fromFuture(getPublicKey)
                .flatMap(response -> {
                    final var keySpec = new X509EncodedKeySpec(response.publicKey().asByteArray(), "RSA");

                    try
                    {
                        final var keyFactory = KeyFactory.getInstance(keySpec.getAlgorithm());

                        return Mono.just(keyFactory.generatePublic(keySpec))
                                .cast(RSAKey.class);
                    }
                    catch(NoSuchAlgorithmException | InvalidKeySpecException e)
                    {
                        return Mono.error(e);
                    }
                })
                .map(Algorithm::RSA256)
                .block();
    }

    @Override
    public void verify(final DecodedJWT jwt) throws SignatureVerificationException
    {
        verifyAlgorithm.verify(jwt);
    }

    @Override
    public byte[] sign(final byte[] contentBytes) throws SignatureGenerationException
    {
        final var sign = Mono.fromFuture(kmsAsyncClient.sign(builder -> builder.keyId(keyId)
                .signingAlgorithm(SigningAlgorithmSpec.RSASSA_PKCS1_V1_5_SHA_256)
                .message(SdkBytes.fromByteArray(contentBytes))));

        return Objects.requireNonNull(sign.block())
                .signature()
                .asByteArray();
    }
}
