package boozilla.houston.gradle;

import boozilla.houston.gradle.task.RunVerifier;
import boozilla.houston.gradle.task.SyncSchema;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import javax.annotation.Nonnull;

/**
 * The HoustonPlugin class represents a plugin that can be applied to a Gradle project.
 * It adds the Houston plugin functionality to the specified project.
 */
public class HoustonPlugin implements Plugin<Project> {
    private static final String GROUP = "houston";

    /**
     * Creates a new instance of HoustonPlugin.
     */
    public HoustonPlugin()
    {
        super();
    }

    /**
     * Applies the Houston plugin to the specified project.
     *
     * @param project The project to which the Houston plugin should be applied.
     */
    @Override
    public void apply(@Nonnull final Project project)
    {
        final var objects = project.getObjects();
        final var container = objects.domainObjectContainer(HoustonEnvironment.class,
                name -> objects.newInstance(HoustonEnvironment.class, name));

        project.getExtensions().add(GROUP, container);

        container.all(environment -> {
            final var tasks = project.getTasks();

            tasks.register(
                    environment.getTaskName("syncSchema"),
                    SyncSchema.class,
                    task -> {
                        task.setGroup(GROUP);
                        task.getEnvironment().set(environment);
                    }
            );

            tasks.register(
                    environment.getTaskName("runVerifier"),
                    RunVerifier.class,
                    task -> {
                        task.setGroup(GROUP);
                        task.getEnvironment().set(environment);

                        final var verifierProject = environment.getVerifier().get();
                        task.dependsOn(verifierProject.getTasks().getByName("classes"));
                    }
            );
        });
    }
}
