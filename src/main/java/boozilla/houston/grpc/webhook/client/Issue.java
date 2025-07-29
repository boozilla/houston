package boozilla.houston.grpc.webhook.client;

import java.util.Collection;
import java.util.Optional;

public interface Issue {
    Optional<String> getUid();

    String getId();

    Collection<String> getLabels();
}
