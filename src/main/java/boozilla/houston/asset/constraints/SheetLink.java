package boozilla.houston.asset.constraints;

import boozilla.houston.asset.AssetLink;
import boozilla.houston.asset.sql.Select;
import boozilla.houston.exception.AssetTypeMismatchException;
import boozilla.houston.exception.AssetVerifyException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class SheetLink extends LocalizedAssetSheetConstraints {
    @Override
    public Optional<String> targetSheetName()
    {
        return Optional.empty();
    }

    @Override
    public Flux<Throwable> check(final AssetAccessor accessor)
    {
        final var updated = accessor.updatedMergeKey();

        return Flux.fromIterable(updated)
                .flatMap(key -> accessor.links(key.sheetName())
                        .flatMap(link -> typeChecking(link, accessor)
                                .switchIfEmpty(nonExists(link, accessor))));
    }

    @Override
    public String subject()
    {
        return message("CONSTRAINTS_SUBJECT_SHEET_LINK");
    }

    private Flux<Throwable> typeChecking(final AssetLink link, final AssetAccessor accessor)
    {
        final var target = link.getRelated();

        final var linkType = accessor.columnType(link.getSheetName(), link.getColumnName());
        final var targetType = accessor.columnType(target.getSheetName(), target.getColumnName());

        if(!linkType.equals(targetType))
        {
            return Flux.just(new AssetTypeMismatchException(link.getSheetName(), link.getColumnName(), linkType,
                    target.getSheetName(), target.getColumnName(), targetType));
        }

        return Flux.empty();
    }

    private Flux<Throwable> nonExists(final AssetLink link, final AssetAccessor accessor)
    {
        final var target = link.getRelated();

        return accessor.query(link, Select.columns(link.getColumnName())
                        .from(link.getSheetName()))
                .flatMap(data -> Flux.fromStream(data.stream(link.getColumnName(), Object.class)))
                .collect(Collectors.toUnmodifiableSet())
                .filter(targetRows -> !targetRows.isEmpty())
                .flatMapMany(linkedValues -> {
                    final var existsQuery = Select.columns(target.getColumnName())
                            .from(target.getSheetName())
                            .where(":COLUMN IN :VALUES")
                            .parameter("COLUMN", target.getColumnName())
                            .parameter("VALUES", linkedValues);

                    return accessor.query(target, existsQuery)
                            .flatMap(data -> Flux.fromStream(data.stream(target.getColumnName(), Object.class)))
                            .collect(Collectors.toUnmodifiableSet())
                            .flatMapMany(exists -> Flux.fromStream(linkedValues.stream().filter(value -> !exists.contains(value))));
                })
                .collect(Collectors.toUnmodifiableSet())
                .flatMapMany(nonExists -> {
                    if(nonExists.isEmpty())
                        return Flux.empty();

                    return accessor.query(link, Select.columns(link.getColumnName())
                                    .from(link.getSheetName())
                                    .where(":COLUMN IN :VALUES")
                                    .parameter("COLUMN", link.getColumnName())
                                    .parameter("VALUES", nonExists))
                            .map(data -> {
                                final var value = data.value(link.getColumnName(), Object.class);
                                final var partition = data.value("partition", String.class);
                                final var sheetName = Stream.of(link.getSheetName(), partition)
                                        .filter(Objects::nonNull)
                                        .collect(Collectors.joining("#"));

                                return new AssetVerifyException(message("CONSTRAINTS_ERROR_LINKED"), sheetName, link.getColumnName(), value);
                            });
                });
    }
}
