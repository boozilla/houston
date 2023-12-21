package boozilla.houston.common;

import com.linecorp.armeria.common.annotation.Nullable;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DependencyInjector implements com.linecorp.armeria.common.DependencyInjector {
    private final ApplicationContext context;

    @Override
    public <T> @Nullable T getInstance(final @NotNull Class<T> type)
    {
        return context.getBean(type);
    }

    @Override
    public void close()
    {
        // Do nothing
    }
}
