package boozilla.houston.security;

import com.auth0.jwt.algorithms.Algorithm;
import lombok.Getter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.kms.KmsAsyncClient;
import software.amazon.awssdk.services.kms.model.SigningAlgorithmSpec;

import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.Objects;

public record KmsAlgorithmProvider(KmsAsyncClient client) {
    public Flux<Algorithm> get(final String keyId)
    {
        return getSigningAlgorithmSpecs(keyId)
                .flatMap(signingSpec -> {
                    final var algoSpec = AlgorithmSpec.findByKmsName(signingSpec.name());

                    if(Objects.isNull(algoSpec))
                    {
                        return Flux.empty();
                    }

                    return Flux.just(new KmsAlgorithm(keyId, algoSpec, signingSpec, client));
                });
    }

    private Flux<SigningAlgorithmSpec> getSigningAlgorithmSpecs(final String keyId)
    {
        final var describeKey = client.describeKey(builder -> builder.keyId(keyId));

        return Mono.fromFuture(describeKey)
                .flatMapMany(response -> {
                    final var metadata = response.keyMetadata();

                    if(!metadata.enabled())
                    {
                        return Flux.error(new RuntimeException("KMS Key is not enabled"));
                    }

                    return Flux.fromIterable(metadata.signingAlgorithms());
                });
    }

    @Getter
    public enum AlgorithmSpec {
        PS256("PS256",
                SigningAlgorithmSpec.RSASSA_PSS_SHA_256.name(),
                "RSASSA-PSS",
                "RSA",
                new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1)),
        PS384("PS384",
                SigningAlgorithmSpec.RSASSA_PSS_SHA_384.name(),
                "RSASSA-PSS",
                "RSA",
                new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA384, 48, 1)),
        PS512("PS512",
                SigningAlgorithmSpec.RSASSA_PSS_SHA_512.name(),
                "RSASSA-PSS",
                "RSA",
                new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA512, 64, 1)),
        RS256("RS256", SigningAlgorithmSpec.RSASSA_PKCS1_V1_5_SHA_256.name(), "SHA256withRSA", "RSA"),
        RS384("RS384", SigningAlgorithmSpec.RSASSA_PKCS1_V1_5_SHA_384.name(), "SHA384withRSA", "RSA"),
        RS512("RS512", SigningAlgorithmSpec.RSASSA_PKCS1_V1_5_SHA_512.name(), "SHA512withRSA", "RSA"),
        ES256("ES256", SigningAlgorithmSpec.ECDSA_SHA_256.name(), "SHA256withECDSA", "EC"),
        ES384("ES384", SigningAlgorithmSpec.ECDSA_SHA_384.name(), "SHA384withECDSA", "EC"),
        ES512("ES512", SigningAlgorithmSpec.ECDSA_SHA_512.name(), "SHA512withECDSA", "EC"),
        ML44("MD44", SigningAlgorithmSpec.ML_DSA_SHAKE_256.name(), "ML-DSA-44", "ML-DSA"),
        ML65("MD65", SigningAlgorithmSpec.ML_DSA_SHAKE_256.name(), "ML-DSA-65", "ML-DSA"),
        ML87("MD87", SigningAlgorithmSpec.ML_DSA_SHAKE_256.name(), "ML-DSA-87", "ML-DSA");

        private final String jwtName;
        private final String kmsName;
        private final String jcaName;
        private final String parameterName;
        private final AlgorithmParameterSpec parameterSpec;

        AlgorithmSpec(final String jwtName,
                      final String kmsName,
                      final String jcaName,
                      final String parameterName)
        {
            this(jwtName, kmsName, jcaName, parameterName, null);
        }

        AlgorithmSpec(final String jwtName,
                      final String kmsName,
                      final String jcaName,
                      final String parameterName,
                      final AlgorithmParameterSpec parameterSpec)
        {
            this.jwtName = jwtName;
            this.kmsName = kmsName;
            this.jcaName = jcaName;
            this.parameterName = parameterName;
            this.parameterSpec = parameterSpec;
        }

        public static AlgorithmSpec findByJwtName(final String name)
        {
            for(final AlgorithmSpec algorithm : values())
            {
                if(algorithm.getJwtName().equals(name))
                {
                    return algorithm;
                }
            }

            return null;
        }

        public static AlgorithmSpec findByKmsName(final String name)
        {
            for(final AlgorithmSpec algorithm : values())
            {
                if(algorithm.getKmsName().equals(name))
                {
                    return algorithm;
                }
            }

            return null;
        }
    }
}
