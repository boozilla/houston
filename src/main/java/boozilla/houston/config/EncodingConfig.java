package boozilla.houston.config;

import boozilla.houston.properties.EncodingProperties;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.common.encoding.StreamEncoderFactories;
import com.linecorp.armeria.common.encoding.StreamEncoderFactory;
import com.linecorp.armeria.server.encoding.EncodingService;
import com.linecorp.armeria.spring.ArmeriaServerConfigurator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "armeria.encoding.enabled", havingValue = "true", matchIfMissing = true)
public class EncodingConfig {
    private static final MediaType GRPC_MEDIA_TYPE = MediaType.create("application", "grpc");

    @Bean
    public ArmeriaServerConfigurator encodingServiceConfigure(final EncodingProperties props)
    {
        return serverBuilder -> {
            if(props.algorithms() == null || props.algorithms().isEmpty())
            {
                return;
            }

            final var factories = props.algorithms().stream()
                    .map(algo -> switch(algo.toLowerCase())
                    {
                        case "gzip" -> StreamEncoderFactories.GZIP;
                        case "deflate" -> StreamEncoderFactories.DEFLATE;
                        default -> throw new IllegalArgumentException("Unsupported encoding algorithm: " + algo);
                    })
                    .toArray(StreamEncoderFactory[]::new);

            serverBuilder.decorator(EncodingService.builder()
                    .encoderFactories(factories)
                    .encodableContentTypes(contentType -> !contentType.is(GRPC_MEDIA_TYPE))
                    .newDecorator());
        };
    }
}
