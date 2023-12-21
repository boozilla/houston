package boozilla.houston.asset;

import org.dhatim.fastexcel.reader.Row;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AssetRow {
    private final AssetCell primary;
    private final Row row;
    private final Map<Integer, AssetCell> cells;

    public AssetRow(final AssetSheet sheet, final Row row)
    {
        this.row = row;
        this.cells = IntStream.rangeClosed(sheet.startOfColumn(), sheet.endOfColumn())
                .mapToObj(index -> {
                    final var cell = row.getOptionalCell(index).orElse(null);
                    return new AssetCell(sheet, this, sheet.column(index), cell);
                })
                .collect(Collectors.toUnmodifiableMap(
                        AssetCell::index,
                        cell -> cell)
                );

        this.primary = cells.values().stream()
                .filter(AssetCell::isPrimary)
                .findAny()
                .orElse(null);
    }

    public AssetCell primary()
    {
        return primary;
    }

    public int index()
    {
        return row.getRowNum();
    }

    protected AssetCell cell(final int index)
    {
        return cells.get(index);
    }

    public Object value(final int index)
    {
        final var cell = cell(index);

        if(Objects.isNull(cell))
            return null;

        return cell.value();
    }

    public boolean endOfRow()
    {
        return Objects.isNull(primary) || primary.isEmpty();
    }

    @Override
    public String toString()
    {
        return cells.values().toString();
    }
}
