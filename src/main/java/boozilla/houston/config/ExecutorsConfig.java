package boozilla.houston.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class ExecutorsConfig {
    private final ScheduledExecutorService scheduledExecutorService;

    public ExecutorsConfig()
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

    @PreDestroy
    public void shutdown()
    {
        scheduledExecutorService.shutdown();
    }
}
