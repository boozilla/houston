package boozilla.houston;

import boozilla.houston.asset.Assets;
import boozilla.houston.grpc.webhook.command.StereotypeCommand;
import boozilla.houston.repository.Vaults;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.data.repository.Repository;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Application {
    private static ApplicationContext context;

    public static void main(final String... args)
    {
        // TODO
        //  gradle-plugin, watcher maven publish 간소화 방법 고민
        //  빌드 자동화
        context = SpringApplication.run(Application.class, args);
    }

    public static MessageSourceAccessor messageSourceAccessor()
    {
        return context.getBean(MessageSourceAccessor.class);
    }

    public static Assets assets()
    {
        return context.getBean(Assets.class);
    }

    public static <T extends Repository<?, ?>> T repository(final Class<T> repositoryClass)
    {
        return context.getBean(repositoryClass);
    }

    public static <T extends StereotypeCommand> T command(final Class<T> commandClass)
    {
        return context.getBean(commandClass);
    }

    public static Vaults vaults()
    {
        return context.getBean(Vaults.class);
    }
}
