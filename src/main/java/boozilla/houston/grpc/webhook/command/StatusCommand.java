package boozilla.houston.grpc.webhook.command;

import boozilla.houston.common.PeriodFormatter;
import boozilla.houston.grpc.webhook.GitBehavior;
import org.joda.time.Period;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Set;

@Component
public class StatusCommand implements Command {
    private final String hostName;
    private final String hostAddress;
    private final long startUp;
    private final MessageSourceAccessor messageSourceAccessor;

    public StatusCommand(final ApplicationContext context, final MessageSourceAccessor messageSourceAccessor)
    {
        try
        {
            this.hostName = InetAddress.getLocalHost().getHostName();
            this.hostAddress = InetAddress.getLocalHost().getHostAddress();
        }
        catch(UnknownHostException e)
        {
            throw new RuntimeException(e);
        }

        this.startUp = context.getStartupDate();
        this.messageSourceAccessor = messageSourceAccessor;
    }

    @Override
    public Set<String> aliases()
    {
        return Set.of("/status");
    }

    @Override
    public String commandTemplate()
    {
        return "/status";
    }

    @Override
    public Mono<Void> run(final String packageName, final String projectId, final String issueId,
                          final String targetRef, final String command, final GitBehavior<?> behavior)
    {
        final var period = new Period(startUp, System.currentTimeMillis());
        final var uptime = PeriodFormatter.print(period);
        final var message = messageSourceAccessor.getMessage("STATUS_COMMAND_MESSAGE")
                .formatted(hostName, hostAddress, uptime);

        return behavior.commentMessage(projectId, issueId, message);
    }

    @Override
    public String description()
    {
        return "HELP_COMMAND_STATUS_DESCRIPTION";
    }

    @Override
    public String options()
    {
        return "HELP_COMMAND_STATUS_OPTIONS";
    }

    @Override
    public Set<String> example()
    {
        return Set.of("HELP_COMMAND_STATUS_EXAMPLE_1");
    }
}
