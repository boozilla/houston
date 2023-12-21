package boozilla.houston.repository;

import com.google.protobuf.InvalidProtocolBufferException;
import houston.vo.asset.Archive;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.core.checksums.Sha256Checksum;
import software.amazon.awssdk.core.internal.async.ByteBuffersAsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.ChecksumAlgorithm;
import software.amazon.awssdk.services.s3.model.ChecksumMode;

import java.util.Base64;
import java.util.HexFormat;

@Component
public class Vaults {
    private final String bucket;
    private final S3AsyncClient client;

    public Vaults(@Value("${s3.bucket}") final String bucket)
    {
        this.bucket = bucket;
        this.client = S3AsyncClient.builder()
                .build();
    }

    public Mono<String> upload(final String path, final Archive archive)
    {
        final var bytes = archive.toByteArray();

        final var checksum = new Sha256Checksum();
        checksum.update(bytes);

        final var checksumBytes = checksum.getChecksumBytes();
        final var checksumHex = HexFormat.of().formatHex(checksumBytes);
        final var checksumBase64 = Base64.getEncoder().encodeToString(checksumBytes);

        final var uploadFuture = client.putObject(builder -> builder
                        .key(path + "/" + checksumHex)
                        .bucket(bucket)
                        .checksumAlgorithm(ChecksumAlgorithm.SHA256)
                        .build(),
                ByteBuffersAsyncRequestBody.from(bytes));

        return Mono.fromFuture(uploadFuture)
                .flatMap(response -> {
                    if(!response.checksumSHA256().contentEquals(checksumBase64))
                    {
                        return Mono.error(new RuntimeException("Checksum mismatch"));
                    }

                    return Mono.just(checksumHex);
                });
    }

    public Mono<Archive> download(final String key)
    {
        final var downloadFuture = client.getObject(builder -> builder
                        .bucket(bucket)
                        .key(key)
                        .checksumMode(ChecksumMode.ENABLED)
                        .build(),
                AsyncResponseTransformer.toBytes());

        return Mono.fromFuture(downloadFuture)
                .flatMap(response -> {
                    try
                    {
                        final var archive = Archive.parseFrom(response.asByteArray());
                        return Mono.just(archive);
                    }
                    catch(InvalidProtocolBufferException e)
                    {
                        return Mono.error(e);
                    }
                });
    }
}
