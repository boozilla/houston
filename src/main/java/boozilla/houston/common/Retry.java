package boozilla.houston.common;

import lombok.experimental.UtilityClass;

import java.time.Duration;

@UtilityClass
public class Retry {
    public reactor.util.retry.RetryBackoffSpec defaultBackoff()
    {
        return reactor.util.retry.Retry.backoff(3, Duration.ofMillis(300));
    }
}
