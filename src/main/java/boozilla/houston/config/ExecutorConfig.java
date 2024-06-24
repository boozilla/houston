package boozilla.houston.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class ExecutorConfig {
    private final ScheduledExecutorService scheduledExecutorService;

    public ExecutorConfig()
    {
        final var threadFactory = Thread.ofVirtual().factory();
        scheduledExecutorService = Executors.newScheduledThreadPool(Integer.MAX_VALUE, threadFactory);
    }

    @Bean
    public ScheduledExecutorService scheduledExecutorService()
    {
        return scheduledExecutorService;
    }

    @Bean
    public ExecutorService executorService()
    {
        return scheduledExecutorService;
    }
}
