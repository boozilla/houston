package boozilla.houston.decorator.auth;

import boozilla.houston.token.AdminApiKey;
import com.linecorp.armeria.common.HttpHeaders;
import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.docs.DocServiceBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

@Slf4j
@Component
public class JwtAdminAuthorizer implements HttpAuthorizer {
    private static final String TOKEN_HEADER_NAME = "x-houston-token";

    private final AdminApiKey adminApiKey;

    public JwtAdminAuthorizer(@Nullable final DocServiceBuilder docServiceBuilder,
                              final AdminApiKey adminApiKey)
    {
        if(Objects.nonNull(docServiceBuilder))
            docServiceBuilder.exampleHeaders(HttpHeaders.of(TOKEN_HEADER_NAME, Strings.EMPTY));

        this.adminApiKey = adminApiKey;
    }

    @Override
    public @Nonnull CompletionStage<Boolean> authorize(@Nonnull final ServiceRequestContext ctx,
                                                       @Nonnull final HttpRequest httpRequest)
    {
        final var header = Optional.ofNullable(httpRequest.headers().get(TOKEN_HEADER_NAME));
        final var query = Optional.ofNullable(ctx.queryParam("token"));
        final var token = header.isEmpty() ? query : header;

        return adminApiKey.verify(token)
                .toFuture();
    }
}
