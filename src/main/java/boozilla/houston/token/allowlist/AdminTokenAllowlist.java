package boozilla.houston.token.allowlist;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AdminTokenAllowlist implements SmartLifecycle {
    private final List<AllowlistSource> sources;
    private final Map<String, SourceState> states = new ConcurrentHashMap<>();
    private final AtomicReference<Set<String>> mergedTokens = new AtomicReference<>(Set.of());
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean polling = new AtomicBoolean(false);
    private final AtomicBoolean failClosed = new AtomicBoolean(true);

    private List<AllowlistSource> activeSources = List.of();
    private List<AllowlistSource> pollingSources = List.of();

    public AdminTokenAllowlist(final List<AllowlistSource> sources)
    {
        this.sources = sources;
    }

    @PostConstruct
    void validateConfiguration()
    {
        activeSources = List.copyOf(sources);

        pollingSources = activeSources.stream()
                .filter(AllowlistSource::isPollingSource)
                .toList();

        activeSources.forEach(AllowlistSource::validateConfiguration);
        activeSources.forEach(source -> states.put(source.name(), new SourceState()));
    }

    public boolean contains(final String token)
    {
        if(failClosed.get())
        {
            return false;
        }

        return mergedTokens.get()
                .contains(token);
    }

    public boolean failClosed()
    {
        return failClosed.get();
    }

    @Override
    public void start()
    {
        loadInitialSources();
        running.set(true);
    }

    @Scheduled(fixedDelay = 1000)
    public void scheduledReload()
    {
        if(!running.get() || !polling.compareAndSet(false, true))
        {
            return;
        }

        try
        {
            reloadPollingSources();
        }
        finally
        {
            polling.set(false);
        }
    }

    @Override
    public void stop()
    {
        running.set(false);
    }

    @Override
    public boolean isRunning()
    {
        return running.get();
    }

    @Override
    public boolean isAutoStartup()
    {
        return true;
    }

    @Override
    public int getPhase()
    {
        return Integer.MAX_VALUE;
    }

    @Override
    public void stop(final Runnable callback)
    {
        stop();
        callback.run();
    }

    private void loadInitialSources()
    {
        for(final var source : activeSources)
        {
            try
            {
                final var snapshot = Objects.requireNonNull(source.loadInitial()
                        .block());
                final var state = states.get(source.name());

                state.tokens.set(snapshot.tokens());
                state.healthy.set(true);
                state.nextSyncAt.set(System.currentTimeMillis() + source.syncIntervalMs());
            }
            catch(final Exception e)
            {
                final var state = states.get(source.name());
                state.healthy.set(false);
                throw new IllegalStateException("Failed to initialize allowlist source [%s]".formatted(source.name()), e);
            }
        }

        refreshMergedTokens();
        refreshFailClosed();
    }

    private void reloadPollingSources()
    {
        final var now = System.currentTimeMillis();

        for(final var source : pollingSources)
        {
            final var state = states.get(source.name());

            if(now < state.nextSyncAt.get())
            {
                continue;
            }

            try
            {
                final var updated = source.reloadIfChanged()
                        .block();
                updated.ifPresent(snapshot -> state.tokens.set(snapshot.tokens()));
                state.healthy.set(true);
            }
            catch(final Exception e)
            {
                state.healthy.set(false);
                log.error("Failed to reload allowlist source [source={}]", source.name(), e);
            }
            finally
            {
                state.nextSyncAt.set(System.currentTimeMillis() + source.syncIntervalMs());
            }
        }

        refreshMergedTokens();
        refreshFailClosed();
    }

    private void refreshMergedTokens()
    {
        final var merged = states.values().stream()
                .flatMap(state -> state.tokens.get().stream())
                .collect(Collectors.toUnmodifiableSet());
        mergedTokens.set(merged);
    }

    private void refreshFailClosed()
    {
        final var shouldClose = activeSources.stream()
                .map(AllowlistSource::name)
                .map(states::get)
                .anyMatch(state -> !state.healthy.get());
        failClosed.set(shouldClose);
    }

    private static class SourceState {
        private final AtomicReference<Set<String>> tokens = new AtomicReference<>(Set.of());
        private final AtomicBoolean healthy = new AtomicBoolean(false);
        private final AtomicReference<Long> nextSyncAt = new AtomicReference<>(0L);
    }
}
