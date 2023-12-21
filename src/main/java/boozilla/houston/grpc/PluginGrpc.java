package boozilla.houston.grpc;

import boozilla.houston.annotation.ScopeService;
import boozilla.houston.annotation.SecuredService;
import boozilla.houston.asset.AssetVerifier;
import boozilla.houston.asset.Assets;
import boozilla.houston.context.ScopeContext;
import com.google.protobuf.Empty;
import houston.grpc.service.AssetSchema;
import houston.grpc.service.ReactorPluginServiceGrpc;
import houston.grpc.service.RunVerifierResponse;
import houston.grpc.service.UploadVerifierRequest;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.tools.shaded.net.bytebuddy.dynamic.loading.ByteArrayClassLoader;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

@SecuredService
@AllArgsConstructor
public class PluginGrpc extends ReactorPluginServiceGrpc.PluginServiceImplBase {
    private final Assets assets;

    @Override
    @ScopeService
    public Flux<AssetSchema> schema(final Empty request)
    {
        final var scope = ScopeContext.get();

        return assets.container().schemas(scope);
    }

    @Override
    public Flux<RunVerifierResponse> runVerifier(final UploadVerifierRequest request)
    {
        final var container = assets.container();
        final var classLoader = new ByteArrayClassLoader(getClass().getClassLoader(), Map.of(
                request.getClassName(), request.getVerifierByteCode().toByteArray()));

        try
        {
            final var constraintsClass = classLoader.loadClass(request.getClassName());
            final var constraints = AssetVerifier.newConstraints(constraintsClass);

            return AssetVerifier.exceptions(container, constraints)
                    .onErrorResume(Flux::just)
                    .map(error -> {
                        final var writer = new StringWriter();

                        try(final var printer = new PrintWriter(writer))
                        {
                            error.printStackTrace(printer);

                            return RunVerifierResponse.newBuilder()
                                    .setStacktrace(writer.toString())
                                    .build();
                        }
                    });
        }
        catch(ClassNotFoundException e)
        {
            return Flux.error(e);
        }
    }
}
