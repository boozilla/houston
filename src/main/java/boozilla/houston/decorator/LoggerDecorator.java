package boozilla.houston.decorator;

import com.linecorp.armeria.server.HttpService;
import com.linecorp.armeria.server.logging.LoggingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Slf4j
@Component
public class LoggerDecorator implements ServiceDecorator {
    private final Function<? super HttpService, LoggingService> service;

    public LoggerDecorator()
    {
        this.service = LoggingService.builder()
                .logger(log)
                .newDecorator();
    }

    @Override
    public HttpService apply(final HttpService httpService)
    {
        return service.apply(httpService);
    }
}