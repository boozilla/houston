package boozilla.houston;

import boozilla.houston.asset.Scope;
import io.grpc.*;
import io.grpc.stub.MetadataUtils;

import java.util.Objects;

public class HoustonChannel extends Channel implements AutoCloseable {
    private static final Metadata.Key<String> HOUSTON_TOKEN_KEY = Metadata.Key.of("x-houston-token", Metadata.ASCII_STRING_MARSHALLER);
    private static final Metadata.Key<String> HOUSTON_SCOPE_KEY = Metadata.Key.of("x-houston-scope", Metadata.ASCII_STRING_MARSHALLER);

    private final ManagedChannel channel;

    public HoustonChannel(final String address, final String token, final Scope scope, final boolean tls)
    {
        this.channel = channel(address, token, scope, tls);
    }

    @Override
    public <RequestT, ResponseT> ClientCall<RequestT, ResponseT> newCall(final MethodDescriptor<RequestT, ResponseT> methodDescriptor, final CallOptions callOptions)
    {
        return channel.newCall(methodDescriptor, callOptions);
    }

    @Override
    public String authority()
    {
        return channel.authority();
    }

    private ManagedChannel channel(final String address, final String token, final Scope scope, final boolean tls)
    {
        final var builder = ManagedChannelBuilder.forTarget(address)
                .intercept(MetadataUtils.newAttachHeadersInterceptor(metadata(token, scope.name())))
                .enableRetry()
                .maxRetryAttempts(3);

        if(tls)
        {
            builder.useTransportSecurity();
        }
        else
        {
            builder.usePlaintext();
        }

        return builder.build();
    }

    private Metadata metadata(final String token, final String scope)
    {
        final var metadata = new Metadata();
        metadata.put(HOUSTON_TOKEN_KEY, token.replaceAll("\n|\\s+", ""));
        metadata.put(HOUSTON_SCOPE_KEY, scope);

        return metadata;
    }

    @Override
    public void close()
    {
        if(Objects.nonNull(channel))
            channel.shutdown();
    }
}
