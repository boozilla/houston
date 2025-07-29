package boozilla.houston.asset.codec;

import boozilla.houston.asset.AssetContainers;
import boozilla.houston.asset.Scope;
import boozilla.houston.context.ScopeContext;
import com.google.protobuf.util.JsonFormat;
import com.linecorp.armeria.common.grpc.GrpcJsonMarshaller;
import houston.grpc.service.AssetQueryResponse;
import io.grpc.MethodDescriptor;
import io.grpc.ServiceDescriptor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public class DynamicJsonMarshaller implements GrpcJsonMarshaller {
    private final GrpcJsonMarshaller defaultMarshaller;

    private Map<Scope, JsonFormat.Printer> printers;

    public static DynamicJsonMarshaller of(final ServiceDescriptor serviceDescriptor,
                                           final AssetContainers assets)
    {
        final var defaultMarshaller = GrpcJsonMarshaller.of(serviceDescriptor);

        return new DynamicJsonMarshaller(defaultMarshaller, assets);
    }

    private DynamicJsonMarshaller(final GrpcJsonMarshaller defaultMarshaller,
                                  final AssetContainers assets)
    {
        this.defaultMarshaller = defaultMarshaller;

        assets.onUpdated(container -> printers = container.jsonPrinters());
    }

    @Override
    public <T> void serializeMessage(final MethodDescriptor.@NotNull Marshaller<T> marshaller,
                                     final @NotNull T message,
                                     final @NotNull OutputStream os) throws IOException
    {
        if(message instanceof final AssetQueryResponse queryResponse)
        {
            final var scope = ScopeContext.get();
            final var printer = printers.get(scope);

            os.write(printer.print(queryResponse)
                    .getBytes());
        }
        else
        {
            defaultMarshaller.serializeMessage(marshaller, message, os);
        }
    }

    @Override
    public <T> @NotNull T deserializeMessage(final MethodDescriptor.@NotNull Marshaller<T> marshaller,
                                             final @NotNull InputStream is) throws IOException
    {
        return defaultMarshaller.deserializeMessage(marshaller, is);
    }
}
