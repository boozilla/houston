package boozilla.houston.asset;

import boozilla.houston.Application;
import boozilla.houston.asset.constraints.AssetSheetConstraints;
import boozilla.houston.grpc.webhook.GitBehavior;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.tools.shaded.net.bytebuddy.dynamic.loading.ByteArrayClassLoader;
import reactor.util.function.Tuples;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;

@Slf4j
public class AssetVerifier {
    private final AssetContainer container;
    private final Set<AssetSheetConstraints> constraints;

    public AssetVerifier(final AssetContainer container, final Set<AssetSheetConstraints> constraints)
    {
        this.container = container;
        this.constraints = constraints;
    }

    public static AssetSheetConstraints newConstraints(final Class<?> constrainsClass)
    {
        try
        {
            return (AssetSheetConstraints) constrainsClass.getConstructor().newInstance();
        }
        catch(NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e)
        {
            throw new RuntimeException("Failed to create Asset constraints object", e);
        }
    }

    public static Flux<Throwable> exceptions(final AssetContainer container, final AssetSheetConstraints... constraints)
    {
        return Flux.fromArray(constraints)
                .flatMap(c -> c.check(container))
                .distinct();
    }

    private Flux<AssetSheetConstraints> scanConstraints(final GitBehavior<?> behavior, final String projectId, final String ref)
    {
        return behavior.allFiles(projectId, ref)
                .flatMapMany(Flux::fromIterable)
                .filter(path -> path.endsWith(".class"))
                .flatMap(path -> {
                    final var className = path.replace(".class", "")
                            .replaceAll("/", ".");

                    return behavior.openFile(projectId, ref, path)
                            .doOnNext(bytes -> log.info("Scan asset verifier class file [path={}]", path))
                            .map(bytes -> Tuples.of(className, bytes));
                })
                .flatMap(tuple -> {
                    final var className = tuple.getT1();
                    final var bytes = tuple.getT2();
                    final var classLoader = new ByteArrayClassLoader(getClass().getClassLoader(), Map.of(className, bytes));

                    try
                    {
                        final var constraintsClass = classLoader.loadClass(className);
                        return Flux.just(newConstraints(constraintsClass));
                    }
                    catch(ClassNotFoundException e)
                    {
                        return Flux.error(e);
                    }
                });
    }

    public Flux<Throwable> exceptions(final GitBehavior<?> behavior, final String projectId, final String issueId, final String ref)
    {
        final var messageAccessor = Application.messageSourceAccessor();

        return Flux.fromIterable(constraints)
                .concatWith(scanConstraints(behavior, projectId, ref))
                .flatMap(c -> c.check(container)
                        .doFirst(() -> behavior.commentMessage(projectId, issueId,
                                        messageAccessor.getMessage("CONSTRAINTS_SUBJECT").formatted(c.subject()))
                                .subscribeOn(Schedulers.boundedElastic())
                                .subscribe()))
                .distinct()
                .doOnNext(error -> error.printStackTrace(System.out));
    }
}
