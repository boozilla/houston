package boozilla.houston.config;

import boozilla.houston.common.AwsCredentialsCondition;
import boozilla.houston.properties.KmsProperties;
import boozilla.houston.security.EcdsaKeyProvider;
import boozilla.houston.security.KmsAlgorithm;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.ECDSAKeyProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.kms.KmsAsyncClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClient;

@Configuration
public class SecurityConfig {
    @Bean
    @Conditional(AwsCredentialsCondition.class)
    public SecretsManagerAsyncClient secretsManagerAsyncClient()
    {
        return SecretsManagerAsyncClient.builder()
                .build();
    }

    @Bean
    @Conditional(AwsCredentialsCondition.class)
    public KmsAsyncClient kmsAsyncClient()
    {
        return KmsAsyncClient.builder()
                .build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "key", name = "ecdsa-path")
    public ECDSAKeyProvider ecdsaKeyProviderFromPath(@Value("${key.ecdsa-path}") final String path)
    {
        return EcdsaKeyProvider.ofPath(path);
    }

    @Bean
    @ConditionalOnProperty(prefix = "key", name = "ecdsa-pkcs8")
    public ECDSAKeyProvider ecdsaKeyProviderFromContent(@Value("${key.ecdsa-pkcs8}") final String pkcs8)
    {
        return EcdsaKeyProvider.ofPkcs8(pkcs8);
    }

    @Bean
    @ConditionalOnBean(ECDSAKeyProvider.class)
    @ConditionalOnProperty(prefix = "key", name = "algorithm", havingValue = "ECDSA256")
    public Algorithm ecdsa256Algorithm(final ECDSAKeyProvider ecdsaKeyProvider)
    {
        return Algorithm.ECDSA256(ecdsaKeyProvider);
    }

    @Bean
    @ConditionalOnBean(ECDSAKeyProvider.class)
    @ConditionalOnProperty(prefix = "key", name = "algorithm", havingValue = "ECDSA384")
    public Algorithm ecdsa384Algorithm(final ECDSAKeyProvider ecdsaKeyProvider)
    {
        return Algorithm.ECDSA384(ecdsaKeyProvider);
    }

    @Bean
    @ConditionalOnBean(ECDSAKeyProvider.class)
    @ConditionalOnProperty(prefix = "key", name = "algorithm", havingValue = "ECDSA512")
    public Algorithm ecdsa512Algorithm(final ECDSAKeyProvider ecdsaKeyProvider)
    {
        return Algorithm.ECDSA512(ecdsaKeyProvider);
    }

    @Bean
    @ConditionalOnProperty(prefix = "key", name = "kms.id")
    public Algorithm kmsAlgorithm(final KmsProperties kmsProperties, final KmsAsyncClient kmsAsyncClient)
    {
        return new KmsAlgorithm(kmsProperties.id(), kmsAsyncClient);
    }
}
