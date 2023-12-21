package boozilla.houston.gradle;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;

import javax.inject.Inject;

/**
 * The HoustonEnvironment class represents a Houston environment configuration.
 */
public abstract class HoustonEnvironment {
    /**
     * The DEFAULT_PATH constant specifies the default file path for assets.
     *
     * <p>
     * The path is set to "src/asset" by default. This path should be used when
     * accessing or saving asset files in the application.
     * </p>
     */
    public static final String DEFAULT_PATH = "src/main/asset";

    /**
     * The name of the variable.
     *
     * <p>This variable holds a string representing the name. It is declared as private and final, meaning
     * its value cannot be modified once assigned. The variable is used to store the name of an object
     * or an entity.</p>
     *
     * <p>It is recommended to use getter method to access the value of this variable. Example:</p>
     * <pre>
     *     public String getName() {
     *         return this.name;
     *     }
     * </pre>
     */
    private final String name;

    /**
     * Constructs a new HoustonEnvironment object with the given name.
     *
     * @param name the name of the Houston environment
     */
    @Inject
    public HoustonEnvironment(final String name)
    {
        this.name = name;
    }

    /**
     * Retrieves the URL property for the Houston environment.
     *
     * @return The URL property as a {@link Property} object.
     */
    @Input
    public abstract Property<String> getUrl();

    /**
     * Gets the token used for authentication.
     *
     * @return The token as a String object.
     */
    @Input
    public abstract Property<String> getToken();

    /**
     * Gets the scope of the authorization.
     *
     * @return The scope as a String object.
     */
    @Input
    public abstract Property<String> getScope();

    /**
     * Retrieves the path of the token.
     *
     * @return the path of the token
     */
    @Input
    public abstract Property<String> getPath();

    /**
     * Returns the value indicating whether the TLS (Transport Layer Security) protocol should be used or not.
     *
     * @return The property representing the flag indicating if TLS should be used.
     */
    @Input
    public abstract Property<Boolean> getTls();

    /**
     * Retrieves the verifier property for the Houston environment.
     *
     * @return The verifier property as a {@link Property} object representing the verifier for the Houston environment.
     */
    @Input
    public abstract Property<Project> getVerifier();

    /**
     * Returns the name of the object.
     *
     * @return the name of the object
     */
    public String getName()
    {
        return name;
    }

    /**
     * Retrieves the task name by concatenating the name of the object and the provided task name.
     *
     * @param taskName the name of the task
     * @return the concatenated task name
     */
    public String getTaskName(final String taskName)
    {
        return "%s.%s".formatted(getName(), taskName);
    }
}
