package boozilla.houston.grpc.webhook.client;

import java.util.List;

public interface Issue {
    String getIid();

    List<String> getLabels();
}
