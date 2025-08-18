package boozilla.houston.security;

import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureGenerationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.linecorp.armeria.internal.shaded.bouncycastle.crypto.digests.SHAKEDigest;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsAsyncClient;
import software.amazon.awssdk.services.kms.model.MessageType;
import software.amazon.awssdk.services.kms.model.SigningAlgorithmSpec;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;

@Slf4j
public class KmsAlgorithm extends Algorithm {
    private final String keyId;
    private final KmsAsyncClient client;
    private final KmsAlgorithmProvider.AlgorithmSpec algorithmSpec;
    private final SigningAlgorithmSpec signingSpec;
    private final PublicKey publicKey;

    KmsAlgorithm(final String keyId,
                 final KmsAlgorithmProvider.AlgorithmSpec algorithmSpec,
                 final SigningAlgorithmSpec signingSpec,
                 final KmsAsyncClient kmsAsyncClient)
    {
        super(algorithmSpec.getJwtName(), algorithmSpec.getKmsName());

        this.client = kmsAsyncClient;
        this.keyId = keyId;
        this.algorithmSpec = algorithmSpec;
        this.signingSpec = signingSpec;
        this.publicKey = getPublicKey(algorithmSpec.getParameterName());

        log.info("KMS algorithm {} created", this.getName());
    }

    private PublicKey getPublicKey(final String parameterName)
    {
        return Mono.fromFuture(client.getPublicKey(builder -> builder.keyId(keyId)))
                .flatMap(response -> {
                    try
                    {
                        final var factory = KeyFactory.getInstance(parameterName);
                        final var spkiDer = response.publicKey().asByteArray();
                        final var keySpec = new X509EncodedKeySpec(spkiDer);

                        return Mono.just(factory.generatePublic(keySpec));
                    }
                    catch(final NoSuchAlgorithmException | InvalidKeySpecException e)
                    {
                        return Mono.error(e);
                    }
                })
                .block();
    }

    private byte[] calculateMu(final byte[] message)
    {
        final var shake = new SHAKEDigest(256);
        shake.update(message, 0, message.length);

        final var mu = new byte[shake.getDigestSize()];
        shake.doFinal(mu, 0, 64);

        return mu;
    }

    @Override
    public String getSigningKeyId()
    {
        return keyId;
    }

    @Override
    public void verify(final DecodedJWT decodedJWT) throws SignatureVerificationException
    {
        final var message = "%s.%s".formatted(decodedJWT.getHeader(), decodedJWT.getPayload());
        final var signature = Base64.getUrlDecoder().decode(decodedJWT.getSignature());

        try
        {
            final var verifier = Signature.getInstance(algorithmSpec.getJcaName());

            if(Objects.nonNull(algorithmSpec.getParameterSpec()))
            {
                verifier.setParameter(algorithmSpec.getParameterSpec());
            }

            final var messageBytes = algorithmSpec.getKmsName().startsWith("ML_DSA") && message.length() > 4096 ?
                    calculateMu(message.getBytes()) : message.getBytes();

            verifier.initVerify(publicKey);
            verifier.update(messageBytes);

            if(!verifier.verify(signature))
            {
                throw new SignatureVerificationException(this);
            }
        }
        catch(final SignatureException | NoSuchAlgorithmException | InvalidKeyException |
                    InvalidAlgorithmParameterException e)
        {
            throw new SignatureVerificationException(this, e);
        }
    }

    @Override
    public byte[] sign(final byte[] bytes) throws SignatureGenerationException
    {
        final var mu = algorithmSpec.getKmsName().startsWith("ML_DSA") && bytes.length > 4096;
        final var messageType = mu ? MessageType.EXTERNAL_MU : MessageType.RAW;
        final var messageBytes = mu ? calculateMu(bytes) : bytes;

        final var sign = client.sign(builder -> builder.keyId(keyId)
                .signingAlgorithm(signingSpec)
                .messageType(messageType)
                .message(SdkBytes.fromByteArray(messageBytes)));

        final var response = Mono.fromFuture(sign)
                .block();

        return Objects.requireNonNull(response)
                .signature()
                .asByteArray();
    }
}
