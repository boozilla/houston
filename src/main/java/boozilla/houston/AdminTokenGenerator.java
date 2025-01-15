package boozilla.houston;

import boozilla.houston.security.KmsAlgorithm;
import boozilla.houston.token.AdminApiKey;
import lombok.AccessLevel;
import lombok.Setter;
import picocli.CommandLine;
import software.amazon.awssdk.services.kms.KmsAsyncClient;

import java.util.Objects;
import java.util.Scanner;

@Setter(AccessLevel.NONE)
@CommandLine.Command
public class AdminTokenGenerator implements Runnable {
    @CommandLine.Option(names = {"--key-id", "-k"}, required = true, description = "KMS key ID")
    private String keyId;

    @CommandLine.Option(names = {"--app-name", "-a"}, required = true, description = "Token issuer")
    private String issuer;

    @CommandLine.Option(names = {"--username", "-u"}, interactive = true, description = "Username")
    private String username;

    public static void main(final String... args)
    {
        new CommandLine(new AdminTokenGenerator())
                .execute(args);
    }

    @Override
    public void run()
    {
        final var kmsAlgorithm = new KmsAlgorithm(keyId, KmsAsyncClient.create());
        final var adminApiKey = new AdminApiKey(issuer, kmsAlgorithm);
        final var adminToken = adminApiKey.create(askUsername())
                .block();

        System.out.println(adminToken);
    }

    private String askUsername()
    {
        if(Objects.nonNull(username))
            return username;

        System.out.print("> Input username: ");
        return new Scanner(System.in).nextLine();
    }
}
