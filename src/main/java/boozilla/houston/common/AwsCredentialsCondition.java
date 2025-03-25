package boozilla.houston.common;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;

import java.util.Objects;

public class AwsCredentialsCondition implements Condition {
    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata)
    {
        try(final var credProvider = DefaultCredentialsProvider.create())
        {
            final var cred = credProvider.resolveCredentials();

            return Objects.nonNull(cred);
        }
        catch(SdkClientException e)
        {
            return false;
        }
    }
}
