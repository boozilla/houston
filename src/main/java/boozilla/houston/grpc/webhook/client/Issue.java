package boozilla.houston.grpc.webhook.client;

import java.util.Collection;

public interface Issue {
    String getId();

    Collection<String> getLabels();
}
