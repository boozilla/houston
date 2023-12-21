package boozilla.houston.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("armeria.grpc")
public record GrpcProperties(
        boolean useBlockingTaskExecutor,
        boolean enableUnframedRequests,
        boolean enableReflection
) {

}
