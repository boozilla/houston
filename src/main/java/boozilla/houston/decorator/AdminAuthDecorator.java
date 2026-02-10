package boozilla.houston.decorator;

import boozilla.houston.decorator.auth.HttpAuthorizer;
import com.linecorp.armeria.common.HttpHeaderNames;
import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.ResponseHeaders;
import com.linecorp.armeria.common.annotation.Nullable;
import com.linecorp.armeria.internal.common.grpc.GrpcStatus;
import com.linecorp.armeria.internal.common.grpc.protocol.GrpcTrailersUtil;
import com.linecorp.armeria.server.HttpService;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.auth.AuthService;
import io.grpc.Status;
import io.grpc.StatusException;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Function;

@Slf4j
public class AdminAuthDecorator implements ServiceDecorator {
    private final Function<? super HttpService, AuthService> service;

    public AdminAuthDecorator(final List<HttpAuthorizer> authorizers)
    {
        this.service = AuthService.builder()
                .add(authorizers)
                .onSuccess(this::authSucceeded)
                .onFailure(this::authFailed)
                .newDecorator();
    }

    public @Nonnull HttpResponse authSucceeded(final HttpService delegate, final ServiceRequestContext ctx,
                                               final HttpRequest req) throws Exception
    {
        return delegate.serve(ctx, req);
    }

    public @Nonnull HttpResponse authFailed(final HttpService delegate, final ServiceRequestContext ctx,
                                            final HttpRequest req, @Nullable final Throwable error)
    {
        log.error("Authentication failed", error);

        final var status = error instanceof StatusException statusException ?
                statusException.getStatus() :
                Status.UNAUTHENTICATED.withDescription("Something went wrong");

        final var headerBuilder = ResponseHeaders.builder()
                .endOfStream(true)
                .add(HttpHeaderNames.STATUS, GrpcStatus.grpcCodeToHttpStatus(status.getCode()).codeAsText())
                .add(HttpHeaderNames.CONTENT_TYPE, "application/grpc+proto");

        GrpcTrailersUtil.addStatusMessageToTrailers(headerBuilder, status.getCode().value(), status.getDescription(), new byte[0]);

        return HttpResponse.of(headerBuilder.build());
    }

    @Override
    public HttpService apply(final HttpService httpService)
    {
        return service.apply(httpService);
    }
}
