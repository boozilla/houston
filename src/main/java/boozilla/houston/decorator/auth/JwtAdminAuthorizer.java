package boozilla.houston.decorator.auth;

import boozilla.houston.HoustonHeaders;
import boozilla.houston.token.AdminApiKey;
import boozilla.houston.token.allowlist.AdminTokenAllowlist;
import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.server.ServiceRequestContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

@Slf4j
@Component
@AllArgsConstructor
public class JwtAdminAuthorizer implements HttpAuthorizer {
    private final AdminApiKey adminApiKey;
    private final AdminTokenAllowlist allowlist;

    @Override
    public @Nonnull CompletionStage<Boolean> authorize(@Nonnull final ServiceRequestContext ctx,
                                                       @Nonnull final HttpRequest httpRequest)
    {
        final var token = Optional.ofNullable(httpRequest.headers().get(HoustonHeaders.TOKEN));

        return adminApiKey.verify(token)
                .map(verified -> verified && token.filter(allowlist::contains).isPresent())
                .toFuture();
    }
}
