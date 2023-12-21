package boozilla.houston.gradle.task;

import boozilla.houston.gradle.HoustonChannelUtils;
import boozilla.houston.gradle.HoustonEnvironment;
import com.google.protobuf.Empty;
import houston.grpc.service.AssetSchema;
import houston.grpc.service.ReactorPluginServiceGrpc;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.CharSequenceEncoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * SyncSchema is an abstract class that extends the DefaultTask class. It provides a method to sync the schema and includes an abstract getter for the environment property.
 */
public abstract class SyncSchema extends DefaultTask {
    /**
     * Constructs a new instance of SyncSchema.
     */
    public SyncSchema()
    {
        super();
    }

    /**
     * Retrieves the property representing the current environment for the application.
     *
     * @return the property containing the HoustonEnvironment enum representing the current environment
     */
    @Input
    public abstract Property<HoustonEnvironment> getEnvironment();

    /**
     * This method is responsible for synchronizing the schema.
     * It is annotated with @TaskAction, indicating that it is an executable task action.
     * The method prints the environment using System.out.println.
     */
    @TaskAction
    public void syncSchema()
    {
        final var environment = getEnvironment().get();
        final var basePath = environment.getPath().getOrElse(HoustonEnvironment.DEFAULT_PATH);
        final var bufferFactory = DefaultDataBufferFactory.sharedInstance;
        final var encoder = CharSequenceEncoder.textPlainOnly();

        schema().parallel()
                .flatMap(schema -> {
                    final var dataBuffer = encoder.encodeValue(schema.getSchema(), bufferFactory, ResolvableType.NONE, null, null);
                    return writeSchema(schema.getName(), dataBuffer, basePath)
                            .doOnRequest(empty -> System.out.println("Downloaded schema = " + schema.getName()));
                })
                .then()
                .block();
    }

    /**
     * This private method is responsible for writing the schema to a file.
     * It takes three parameters: name (String), dataBuffer (DataBuffer), and basePath (String).
     * The method uses the provided name to construct the path to the file.
     * It then writes the dataBuffer to the file using Flux and DataBufferUtils.write().
     * The file is opened with options to write, truncate existing content, and create if it doesn't exist.
     * After writing the data to the file, the method releases the dataBuffer.
     * The method returns a Mono<Void> to indicate completion.
     */
    private Mono<Void> writeSchema(final String name, final DataBuffer dataBuffer, final String basePath)
    {
        final var path = Path.of(getProject().getProjectDir().getAbsolutePath(), basePath, name + ".proto");

        return DataBufferUtils.write(
                        Flux.just(dataBuffer),
                        path,
                        StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
                .doOnTerminate(() -> DataBufferUtils.release(dataBuffer));
    }

    /**
     * Retrieves the asset schema from the given environment.
     *
     * @return A Flux emitting AssetSchema objects.
     */
    private Flux<AssetSchema> schema()
    {
        return HoustonChannelUtils.flux(getEnvironment().get(), channel -> {
            final var stub = ReactorPluginServiceGrpc.newReactorStub(channel);
            return stub.schema(Empty.getDefaultInstance());
        });
    }
}
