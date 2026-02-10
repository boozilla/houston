package boozilla.houston.grpc.webhook.client.console;

import boozilla.houston.grpc.webhook.client.Issue;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public record ConsoleIssue(
        String id,
        Collection<String> labels
) implements Issue {
    public ConsoleIssue()
    {
        this("0", List.of());
    }

    @Override
    public Optional<String> getUid()
    {
        return Optional.of(id);
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public Collection<String> getLabels()
    {
        return labels;
    }
}
