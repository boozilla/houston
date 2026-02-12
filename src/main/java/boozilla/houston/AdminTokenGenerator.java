package boozilla.houston;

import boozilla.houston.security.EcdsaKeyProvider;
import boozilla.houston.security.KmsAlgorithmProvider;
import boozilla.houston.token.AdminApiKey;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.AccessLevel;
import lombok.Setter;
import picocli.CommandLine;
import software.amazon.awssdk.services.kms.KmsAsyncClient;

import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.function.Supplier;

@Setter(AccessLevel.NONE)
@CommandLine.Command
public class AdminTokenGenerator implements Runnable {
    @CommandLine.Option(names = {"--issuer", "--iss"}, required = true, defaultValue = "${env:ISSUER}", description = "Token issuer")
    private String issuer;
    @CommandLine.Option(names = {"--username", "--user"}, interactive = true, defaultValue = "${env:USERNAME}", description = "Username")
    private String username;

    // KMS options
    @CommandLine.Option(names = {"--kms-key-id", "--kms"}, defaultValue = "${env:KMS_KEY_ID}", description = "KMS key ID")
    private String kmsKeyId;
    @CommandLine.Option(names = {"--kms-algo"}, defaultValue = "${env:KMS_ALGORITHM}", description = "KMS algorithm")
    private String kmsAlgorithm;

    // Key options
    @CommandLine.Option(names = {"--key-algo", "--algo"}, defaultValue = "${env:KEY_ALGORITHM}", description = "Key algorithm")
    private String keyAlgorithm;
    @CommandLine.Option(names = {"--key-file", "--key"}, defaultValue = "${env:KEY_FILE}", description = "Key file")
    private String keyFile;
    @CommandLine.Option(names = {"--key-pkcs8", "--pkcs8"}, defaultValue = "${env:KEY_PKCS8}", description = "Key PKCS#8 text")
    private String keyPkcs8;

    public static void main(final String... args)
    {
        new CommandLine(new AdminTokenGenerator())
                .execute(args);
    }

    @Override
    public void run()
    {
        final var algorithm = algorithm();
        final var adminApiKey = new AdminApiKey(issuer, List.of(algorithm));
        final var adminToken = adminApiKey.create(askUsername(), algorithm)
                .block();

        System.out.println(adminToken);
    }

    private Algorithm algorithm()
    {
        if(Objects.nonNull(kmsKeyId) && Objects.nonNull(kmsAlgorithm))
        {
            return kmsAlgorithm(kmsKeyId, kmsAlgorithm);
        }

        if(Objects.nonNull(keyAlgorithm))
        {
            if(Objects.nonNull(keyFile))
            {
                return keyAlgorithmFromFile(keyFile, keyAlgorithm);
            }

            if(Objects.nonNull(keyPkcs8))
            {
                return keyAlgorithmFromPkcs8(keyPkcs8, keyAlgorithm);
            }
        }

        throw new IllegalArgumentException("No key option: specify --kms/--kms-algo or --algo with --key/--pkcs8");
    }

    private KmsAlgorithmProvider kmsAlgorithmProvider()
    {
        return new KmsAlgorithmProvider(KmsAsyncClient.create());
    }

    private Algorithm kmsAlgorithm(final String kmsKeyId, final String algorithm)
    {
        final var algorithmSpec = KmsAlgorithmProvider.AlgorithmSpec.findByJwtName(algorithm);

        if(Objects.isNull(algorithmSpec))
        {
            throw new IllegalArgumentException("Unsupported algorithm");
        }

        final var kmsAlgorithmProvider = kmsAlgorithmProvider();

        return kmsAlgorithmProvider.get(kmsKeyId)
                .filter(a -> a.getName().contentEquals(algorithmSpec.getJwtName()))
                .blockFirst();
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
}
