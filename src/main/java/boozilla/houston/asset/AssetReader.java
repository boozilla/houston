package boozilla.houston.asset;

import org.dhatim.fastexcel.reader.ReadableWorkbook;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class AssetReader {
    private final ReadableWorkbook workbook;

    private AssetReader(final InputStream in) throws IOException
    {
        workbook = new ReadableWorkbook(in);
    }

    public static Mono<AssetReader> of(final String filename)
    {
        try
        {
            return of(new FileInputStream(filename));
        }
        catch(FileNotFoundException e)
        {
            throw new RuntimeException("Errors generating file streams", e);
        }
    }

    public static Mono<AssetReader> of(final InputStream in)
    {
        return Mono.fromSupplier(() -> {
                    try
                    {
                        return new AssetReader(in);
                    }
                    catch(IOException e)
                    {
                        throw new RuntimeException("Error reading asset data file", e);
                    }
                })
                .onErrorStop()
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<AssetSheet> sheets()
    {
        return Flux.fromStream(workbook.getSheets())
                .map(AssetSheet::new);
    }
}
