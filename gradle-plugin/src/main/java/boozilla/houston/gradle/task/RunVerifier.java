package boozilla.houston.gradle.task;

import boozilla.houston.asset.constraints.AssetSheetConstraints;
import boozilla.houston.gradle.HoustonChannelUtils;
import boozilla.houston.gradle.HoustonEnvironment;
import com.google.protobuf.ByteString;
import houston.grpc.service.ReactorPluginServiceGrpc;
import houston.grpc.service.RunVerifierResponse;
import houston.grpc.service.UploadVerifierRequest;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;
import reactor.core.publisher.Flux;
import reactor.tools.shaded.net.bytebuddy.dynamic.loading.ByteArrayClassLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The RunVerifier class represents a task that runs a verifier in a Houston environment.
 */
public abstract class RunVerifier extends DefaultTask {
    /**
     * Retrieves the Houston environment associated with this object.
     *
     * @return The Houston environment as a {@link Property} object.
     */
    @Input
    public abstract Property<HoustonEnvironment> getEnvironment();

    /**
     * Retrieves the targetClass property of the Houston environment.
     *
     * @return The targetClass property as a {@link Property} object.
     */
    @Input
    @Optional
    @Option(option = "target-class", description = "The target class to which the option is applied")
    public abstract Property<String> getTargetClass();

    /**
     * Retrieves the target classes based on the targetClass property of the Houston environment.
     * Returns a set of class names as strings.
     *
     * @return The set of target classes as a Set of Strings.
     */
    @Internal
    public Set<String> getTargetClasses()
    {
        final var value = getTargetClass().getOrNull();

        if(Objects.isNull(value))
        {
            return Set.of();
        }

        return Set.of(value.split(","));
    }

    /**
     * Method to run the verifier.
     * <p>
     * This method retrieves the verifier and project from the `HoustonEnvironment` and scans the verifier classes from the project's build directory.
     * It filters the verifier classes to find the classes implementing the `AssetSheetConstraints` interface.
     * Then it runs the verifier for each class.
     * The result is printed to the console.
     */
    @TaskAction
    public void runVerifier()
    {
        final var environment = getEnvironment().get();
        final var project = environment.getVerifier().get();
        final var target = getTargetClasses();
        final var classes = scanVerifierClasses(project);
        final var classLoader = new ByteArrayClassLoader(getClass().getClassLoader(), classes);
        final var verifierClassNames = classes.keySet().stream()
                .filter(name -> {
                    if(!target.isEmpty() && target.stream().noneMatch(name::endsWith))
                    {
                        return false;
                    }

                    try
                    {
                        final var verifierClass = classLoader.loadClass(name);
                        return Arrays.stream(verifierClass.getInterfaces())
                                .anyMatch(interfaceClass -> interfaceClass == AssetSheetConstraints.class);
                    }
                    catch(ClassNotFoundException e)
                    {
                        throw new RuntimeException(e);
                    }
                });

        Flux.fromStream(verifierClassNames)
                .flatMap(name -> run(name, classes.get(name)))
                .doOnNext(response -> System.out.println(response.getStacktrace()))
                .blockLast();
    }

    /**
     * Runs the verifier for a given class with the provided bytecode.
     *
     * @param className The name of the class to run the verifier for.
     * @param bytecode  The bytecode of the class to run the verifier with.
     * @return A Flux of RunVerifierResponse, representing the responses from running the verifier.
     */
    private Flux<RunVerifierResponse> run(final String className, final byte[] bytecode)
    {
        return HoustonChannelUtils.flux(getEnvironment().get(), channel -> {
            final var stub = ReactorPluginServiceGrpc.newReactorStub(channel);
            return stub.runVerifier(UploadVerifierRequest.newBuilder()
                    .setClassName(className)
                    .setVerifierByteCode(ByteString.copyFrom(bytecode))
                    .build());
        });
    }

    /**
     * Scans the verifier classes in the given project and returns a map
     * of class names to their corresponding bytecode.
     *
     * @param project The project to scan for verifier classes.
     * @return A map of class names to their corresponding bytecode.
     */
    private Map<String, byte[]> scanVerifierClasses(final Project project)
    {
        final var buildDir = project.getLayout().getBuildDirectory().dir("classes/java/main").get();
        final var buildDirPath = buildDir.getAsFile().getPath();
        final var buildDirTree = buildDir.getAsFileTree();

        return buildDirTree.matching(patternSet -> patternSet.include(buildDirPath, "/**/*.class"))
                .getFiles()
                .stream()
                .collect(Collectors.toUnmodifiableMap(
                        file -> {
                            final var path = file.getPath();
                            return path.substring(buildDirPath.length() + 1, path.length() - 6).replace('/', '.');
                        },
                        file -> {
                            try
                            {
                                return Files.readAllBytes(file.toPath());
                            }
                            catch(IOException e)
                            {
                                throw new RuntimeException(e);
                            }
                        }
                ));
    }
}
