package boozilla.houston.console;

import boozilla.houston.grpc.webhook.command.Commands;
import boozilla.houston.repository.DataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "console.enabled", havingValue = "true")
public class ConsoleRunner implements ApplicationRunner {
    private final Commands commands;
    private final ConsoleBehaviorFactory behaviorFactory;
    private final DataRepository dataRepository;
    private final String packageName;
    private final String targetBranch;
    private final String projectId;

    public ConsoleRunner(final Commands commands,
                         final ConsoleBehaviorFactory behaviorFactory,
                         final DataRepository dataRepository,
                         @Value("${package-name}") final String packageName,
                         @Value("${branch}") final String targetBranch,
                         @Value("${console.project-id:console}") final String projectId)
    {
        this.commands = commands;
        this.behaviorFactory = behaviorFactory;
        this.dataRepository = dataRepository;
        this.packageName = packageName;
        this.targetBranch = targetBranch;
        this.projectId = projectId;
    }

    @Override
    public void run(final ApplicationArguments args)
    {
        final var behavior = behaviorFactory.create();

        if(behavior.isEmpty())
        {
            log.warn("Console REPL disabled: no Git provider configured (set github.access-token or gitlab.url + gitlab.access-token)");
            return;
        }

        final var repl = new ConsoleRepl(commands, behavior.get(), packageName, targetBranch, projectId, dataRepository);
        final var thread = new Thread(repl::run, "console-repl");
        thread.setDaemon(true);
        thread.start();
    }
}
