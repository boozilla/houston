package boozilla.houston.config;

import boozilla.houston.properties.AwsProperties;
import boozilla.houston.properties.KmsProperties;
import boozilla.houston.security.KmsRsaAlgorithm;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.kms.KmsAsyncClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClient;

import java.util.Objects;

@Configuration
public class SecurityConfig {
    @Bean
    public SecretsManagerAsyncClient secretsManagerAsyncClient(final AwsProperties awsProperties)
    {
        final var builder = SecretsManagerAsyncClient.builder();

        if(Objects.nonNull(awsProperties.endpoint()))
            builder.endpointOverride(awsProperties.endpoint());

        if(Objects.nonNull(awsProperties.accessKey()) && Objects.nonNull(awsProperties.secretKey()))
            builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(awsProperties.accessKey(), awsProperties.secretKey()))
            );

        if(Objects.nonNull(awsProperties.region()))
            builder.region(awsProperties.region());

        return builder.build();
    }

    @Bean
    public KmsAsyncClient kmsAsyncClient(final AwsProperties awsProperties)
    {
        final var builder = KmsAsyncClient.builder();

        if(Objects.nonNull(awsProperties.endpoint()))
            builder.endpointOverride(awsProperties.endpoint());

        if(Objects.nonNull(awsProperties.accessKey()) && Objects.nonNull(awsProperties.secretKey()))
            builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(awsProperties.accessKey(), awsProperties.secretKey()))
            );

        if(Objects.nonNull(awsProperties.region()))
            builder.region(awsProperties.region());

        return builder.build();
    }

    @Bean
    public KmsRsaAlgorithm adminJwtAlgorithm(final KmsProperties kmsProperties, final KmsAsyncClient kmsAsyncClient)
    {
        return new KmsRsaAlgorithm(kmsProperties.adminKey(), kmsAsyncClient);
    }
}
