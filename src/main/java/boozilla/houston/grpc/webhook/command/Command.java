package boozilla.houston.grpc.webhook.command;

import java.util.Set;

public interface Command extends StereotypeCommand {
    String description();

    String options();

    Set<String> example();
}
