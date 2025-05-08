package boozilla.houston;

import boozilla.houston.security.EcdsaKeyProvider;
import boozilla.houston.security.KmsAlgorithm;
import boozilla.houston.token.AdminApiKey;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.AccessLevel;
import lombok.Setter;
import picocli.CommandLine;
import software.amazon.awssdk.services.kms.KmsAsyncClient;

import java.util.Objects;
import java.util.Scanner;
import java.util.function.Supplier;

@Setter(AccessLevel.NONE)
@CommandLine.Command
public class AdminTokenGenerator implements Runnable {
    @CommandLine.Option(names = {"--issuer", "--iss"}, required = true, description = "Token issuer")
    private String issuer;

    @CommandLine.Option(names = {"--username", "--user"}, interactive = true, description = "Username")
    private String username;

    @CommandLine.ArgGroup(multiplicity = "1")
    KeyOrKmsOption keyOrKmsOption;

    public static void main(final String... args)
    {
        new CommandLine(new AdminTokenGenerator())
                .execute(args);
    }

    static class KeyOrKmsOption {
        @CommandLine.Option(names = {"--kms-key-id", "--kms"}, description = "KMS key ID")
        private String kmsKeyId;

        @CommandLine.ArgGroup(exclusive = false)
        private KeyOption keyOption;
    }

    @Override
    public void run()
    {
        final var algorithm = algorithm();
        final var adminApiKey = new AdminApiKey(issuer, algorithm);
        final var adminToken = adminApiKey.create(askUsername())
                .block();

        System.out.println(adminToken);
    }

    private Algorithm algorithm()
    {
        if(Objects.nonNull(keyOrKmsOption.kmsKeyId))
        {
            return kmsAlgorithm(keyOrKmsOption.kmsKeyId);
        }

        if(Objects.nonNull(keyOrKmsOption.keyOption))
        {
            if(Objects.nonNull(keyOrKmsOption.keyOption.keyProvideOption.keyFile))
            {
                return keyAlgorithmFromFile(keyOrKmsOption.keyOption.keyProvideOption.keyFile, keyOrKmsOption.keyOption.algorithm);
            }

            if(Objects.nonNull(keyOrKmsOption.keyOption.keyProvideOption.keyPkcs8))
            {
                return keyAlgorithmFromPkcs8(keyOrKmsOption.keyOption.keyProvideOption.keyPkcs8, keyOrKmsOption.keyOption.algorithm);
            }
        }

        throw new IllegalArgumentException("No key option");
    }

    private Algorithm kmsAlgorithm(final String kmsKeyId)
    {
        return new KmsAlgorithm(kmsKeyId, KmsAsyncClient.create());
    }

    private Algorithm keyAlgorithmFromFile(final String keyFile, final String algorithm)
    {
        return keyAlgorithm(algorithm, () -> EcdsaKeyProvider.ofPath(keyFile));
    }

    private Algorithm keyAlgorithmFromPkcs8(final String pkcs8, final String algorithm)
    {
        return keyAlgorithm(algorithm, () -> EcdsaKeyProvider.ofPkcs8(pkcs8));
    }

    private Algorithm keyAlgorithm(final String algorithm, final Supplier<EcdsaKeyProvider> supplier)
    {
        return switch(algorithm)
        {
            case "ECDSA256" -> Algorithm.ECDSA256(ecdsaKeyProvider(supplier));
            case "ECDSA384" -> Algorithm.ECDSA384(ecdsaKeyProvider(supplier));
            case "ECDSA512" -> Algorithm.ECDSA512(ecdsaKeyProvider(supplier));
            default -> throw new IllegalArgumentException("Unsupported algorithm");
        };
    }

    private EcdsaKeyProvider ecdsaKeyProvider(final Supplier<EcdsaKeyProvider> supplier)
    {
        return supplier.get();
    }

    private String askUsername()
    {
        if(Objects.nonNull(username))
            return username;

        System.out.print("> Input username: ");
        return new Scanner(System.in).nextLine();
    }

    static class KeyOption {
        @CommandLine.ArgGroup(multiplicity = "1")
        KeyProvideOption keyProvideOption;
        @CommandLine.Option(names = {"--key-algo", "--algo"}, required = true, description = "Key algorithm")
        private String algorithm;

        static class KeyProvideOption {
            @CommandLine.Option(names = {"--key-file", "--key"}, description = "Key file")
            private String keyFile;

            @CommandLine.Option(names = {"--key-pkcs8", "--pkcs8"}, description = "Key PKCS#8 text")
            private String keyPkcs8;
        }
    }
}
