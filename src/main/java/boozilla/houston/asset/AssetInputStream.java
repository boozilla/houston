package boozilla.houston.asset;

import boozilla.houston.asset.converter.XmlConverter;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class AssetInputStream extends ByteArrayInputStream {
    private AssetInputStream(final byte[] buf)
    {
        super(buf);
    }

    public static Mono<AssetInputStream> open(final String path, final byte[] buf)
    {
        if(path.endsWith(".xml"))
        {
            try(final var in = new ByteArrayInputStream(buf))
            {
                return XmlConverter.toByteArray(in)
                        .map(AssetInputStream::new);
            }
            catch(IOException e)
            {
                return Mono.error(e);
            }
        }

        return Mono.just(new AssetInputStream(buf));
    }
}
