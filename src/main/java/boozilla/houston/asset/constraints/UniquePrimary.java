package boozilla.houston.asset.constraints;

import boozilla.houston.asset.sql.Select;
import boozilla.houston.exception.AssetVerifyException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.PrintWriter;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class UniquePrimary extends LocalizedAssetSheetConstraints {
    @Override
    public Optional<String> targetSheetName()
    {
        return Optional.empty();
    }

    @Override
    public String subject()
    {
        return message("CONSTRAINTS_SUBJECT_UNIQUE_PRIMARY");
    }

    @Override
    public Flux<Throwable> check(final PrintWriter writer, final AssetAccessor accessor)
    {
        final var updated = accessor.updatedKey()
                .stream()
                .collect(Collectors.toMap(AssetAccessor.Key::sheetName,
                        Function.identity(),
                        (existing, replacement) -> existing))
                .values();

        return Flux.fromIterable(updated).flatMap(key -> accessor.query(key.scope(),
                        Select.columns("code").from(key.sheetName()))
                .collect(Collectors.groupingBy(data -> data.value("code", Long.class), Collectors.counting()))
                .flatMapMany(counts -> Flux.fromIterable(counts.entrySet())
                        .filter(entry -> entry.getValue() > 1)
                        .map(Map.Entry::getKey)
                        .map(duplicateCode -> new AssetVerifyException(message("CONSTRAINTS_ERROR_UNIQUE_PRIMARY"), key.sheetName(), "code", duplicateCode))));
    }
}
