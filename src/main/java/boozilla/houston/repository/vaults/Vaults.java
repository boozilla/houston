package boozilla.houston.repository.vaults;

import boozilla.houston.entity.Data;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import reactor.core.publisher.Mono;

public interface Vaults {
    Mono<UploadResult> upload(final String sheetName, final byte[] content);

    Mono<byte[]> download(final Data data);

    record UploadResult(String path, String checksum) {
        @JsonCreator
        public UploadResult(@JsonProperty("url") final String path,
                            @JsonProperty("sha") final String checksum)
        {
            this.path = path;
            this.checksum = checksum;
        }
    }
}
