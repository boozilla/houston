package boozilla.houston.repository.vaults;

import boozilla.houston.entity.Data;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.checksums.DefaultChecksumAlgorithm;
import software.amazon.awssdk.checksums.SdkChecksum;
import software.amazon.awssdk.core.BytesWrapper;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.core.internal.async.ByteBuffersAsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.ChecksumAlgorithm;
import software.amazon.awssdk.services.s3.model.ChecksumMode;

import java.util.Base64;
import java.util.HexFormat;

public class S3Vaults implements Vaults {
    private final String bucket;
    private final S3AsyncClient client;

    public S3Vaults(final String bucket)
    {
        this.bucket = bucket;
        this.client = S3AsyncClient.builder()
                .build();
    }

    public Mono<UploadResult> upload(final String sheetName, final byte[] content)
    {
        final var checksum = SdkChecksum.forAlgorithm(DefaultChecksumAlgorithm.SHA256);
        checksum.update(content);

        final var checksumBytes = checksum.getChecksumBytes();
        final var checksumHex = HexFormat.of().formatHex(checksumBytes);
        final var checksumBase64 = Base64.getEncoder().encodeToString(checksumBytes);
        final var key = sheetName + "/" + checksumHex;

        final var uploadFuture = client.putObject(builder -> builder
                        .key(key)
                        .bucket(bucket)
                        .checksumAlgorithm(ChecksumAlgorithm.SHA256)
                        .build(),
                ByteBuffersAsyncRequestBody.from(content));

        return Mono.fromFuture(uploadFuture)
                .flatMap(response -> {
                    if(!response.checksumSHA256().contentEquals(checksumBase64))
                    {
                        return Mono.error(new RuntimeException("Checksum mismatch"));
                    }

                    return Mono.just(new UploadResult(key, checksumHex));
                });
    }

    public Mono<byte[]> download(final Data data)
    {
        final var downloadFuture = client.getObject(builder -> builder
                        .bucket(bucket)
                        .key(data.getPath())
                        .checksumMode(ChecksumMode.ENABLED)
                        .build(),
                AsyncResponseTransformer.toBytes());

        return Mono.fromFuture(downloadFuture)
                .map(BytesWrapper::asByteArray);
    }
}
