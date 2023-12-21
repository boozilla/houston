package boozilla.houston.decorator;

import com.linecorp.armeria.server.HttpService;

import java.util.function.Function;

public interface ServiceDecorator extends Function<HttpService, HttpService> {
    static ServiceDecorator empty()
    {
        return httpService -> httpService;
    }

    default ServiceDecorator andThen(final ServiceDecorator after)
    {
        return it -> after.apply(apply(it));
    }
}
