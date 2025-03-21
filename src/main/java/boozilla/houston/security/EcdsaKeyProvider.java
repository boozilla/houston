package boozilla.houston.security;

import com.auth0.jwt.interfaces.ECDSAKeyProvider;
import com.linecorp.armeria.internal.shaded.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.*;
import java.util.Base64;
import java.util.Objects;

@Slf4j
public class EcdsaKeyProvider implements ECDSAKeyProvider {
    private final ECPrivateKey privateKey;
    private final ECPublicKey publicKey;
    private final String privateKeyId;

    private EcdsaKeyProvider(final EncodedKeySpec privateKeySpec)
    {
        this.privateKeyId = generateKeyId(privateKeySpec.getEncoded());
        log.info("Initialized Private Key ID: {}", this.privateKeyId);

        try
        {
            final var keyFactory = KeyFactory.getInstance("EC");
            privateKey = (ECPrivateKey) keyFactory.generatePrivate(privateKeySpec);

            final var params = privateKey.getParams();
            final var publicPoint = calcPublicPoint(params);
            final var publicKeySpec = new ECPublicKeySpec(publicPoint, params);
            publicKey = (ECPublicKey) keyFactory.generatePublic(publicKeySpec);
        }
        catch(NoSuchAlgorithmException e)
        {
            log.error("EC algorithm not available", e);
            throw new RuntimeException("EC algorithm not available", e);
        }
        catch(InvalidKeySpecException e)
        {
            log.error("Invalid key specification", e);
            throw new RuntimeException("Invalid key specification", e);
        }
        catch(Exception e)
        {
            log.error("An unexpected error occurred", e);
            throw new RuntimeException("An unexpected error occurred", e);
        }
    }

    public static EcdsaKeyProvider ofPath(final String path)
    {
        try
        {
            final var expandedPath = expandPath(path);
            final var keyContent = Files.readString(expandedPath);
            final var keyBytes = decodePemKey(keyContent, "PRIVATE");
            final var keySpec = new PKCS8EncodedKeySpec(keyBytes);

            return new EcdsaKeyProvider(keySpec);
        }
        catch(IOException e)
        {
            log.error("Error reading private key file: {}", path, e);
            throw new RuntimeException("Failed to read private key file: " + path, e);
        }
    }

    public static EcdsaKeyProvider ofPkcs8(final String pkcs8)
    {
        final var decoded = Base64.getDecoder().decode(pkcs8);
        final var keySpec = new PKCS8EncodedKeySpec(decoded);

        return new EcdsaKeyProvider(keySpec);
    }

    private static Path expandPath(final String path)
    {
        Objects.requireNonNull(path, "Path must not be null");

        final var normalizedPath = path.trim();

        if(normalizedPath.startsWith("~/") || normalizedPath.startsWith("~\\"))
        {
            final var homeDir = System.getProperty("user.home");
            final var relativePath = normalizedPath.substring(2);
            return Paths.get(homeDir).resolve(relativePath).normalize();
        }

        return Paths.get(normalizedPath).normalize();
    }

    private static byte[] decodePemKey(final String pemContent, final String header)
    {
        Objects.requireNonNull(pemContent, "PEM content must not be null");

        final var stripped = pemContent
                .replace("-----BEGIN %s KEY-----".formatted(header), "")
                .replace("-----END %s KEY-----".formatted(header), "")
                .replaceAll("\\s+", "");

        if(stripped.isEmpty())
        {
            throw new RuntimeException("PEM content is empty after stripping headers and whitespace.");
        }

        return Base64.getDecoder().decode(stripped);
    }

    @Override
    public ECPublicKey getPublicKeyById(final String keyId)
    {
        return this.publicKey;
    }

    @Override
    public ECPrivateKey getPrivateKey()
    {
        return this.privateKey;
    }

    @Override
    public String getPrivateKeyId()
    {
        return this.privateKeyId;
    }

    private String generateKeyId(final byte[] keyBytes)
    {
        try
        {
            final var digest = MessageDigest.getInstance("SHA-256");
            final var hash = digest.digest(keyBytes);
            final var hexStringBuilder = new StringBuilder();

            for(final var b : hash)
            {
                hexStringBuilder.append(String.format("%02x", b));
            }

            return hexStringBuilder.substring(0, 16);
        }
        catch(NoSuchAlgorithmException e)
        {
            log.error("Failed to generate private key id", e);
            throw new RuntimeException("Failed to generate private key id", e);
        }
    }

    private ECPoint calcPublicPoint(final ECParameterSpec params)
    {
        final var publicPoint = EC5Util.convertSpec(params)
                .getG()
                .multiply(privateKey.getS())
                .normalize();

        return new ECPoint(publicPoint.getAffineXCoord().toBigInteger(),
                publicPoint.getAffineYCoord().toBigInteger());
    }
}
