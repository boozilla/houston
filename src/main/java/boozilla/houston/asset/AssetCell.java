package boozilla.houston.asset;

import boozilla.houston.Application;
import boozilla.houston.exception.AssetCellException;
import org.dhatim.fastexcel.reader.Cell;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class AssetCell {
    private final AssetSheet sheet;
    private final AssetRow row;
    private final AssetColumn column;
    private final Cell cell;

    public AssetCell(final AssetSheet sheet, final AssetRow row, final AssetColumn column, final Cell cell)
    {
        this.sheet = sheet;
        this.row = row;
        this.column = column;
        this.cell = cell;
    }

    public int index()
    {
        return column.index();
    }

    public Object value()
    {
        return column.array() ? arrayValue() : singleValue();
    }

    private List<Object> arrayValue()
    {
        if(Objects.isNull(cell))
            return List.of();

        final var arrayString = cell.getText();
        final var stream = arrayString.isEmpty() ? Stream.<String>of() :
                Arrays.stream(arrayString.replaceAll("\\[", "")
                        .replaceAll("]", "")
                        .split("\\s*,\\s*"));
        final var list = stream.map(value -> {
                    try
                    {
                        return column.type().cast(value.trim().replaceAll("^\"|\"$", ""));
                    }
                    catch(Exception e)
                    {
                        throw cellException("READ_SHEET_ERROR_TYPE_CONVERT", e);
                    }
                })
                .toList();

        if(!column.isNullable() && list.isEmpty())
        {
            throw cellException("READ_SHEET_ERROR_REQUIRE_COLUMN");
        }

        return list;
    }

    private Object singleValue()
    {
        if(Objects.isNull(cell) || cell.getText().isEmpty())
        {
            if(!column.isNullable())
            {
                throw cellException("READ_SHEET_ERROR_REQUIRE_COLUMN");
            }
            else
                return null;
        }

        try
        {
            return column.type().extract(cell);
        }
        catch(Exception e)
        {
            throw cellException("READ_SHEET_ERROR_TYPE_CONVERT", e);
        }
    }

    private RuntimeException cellException(final String messageCode)
    {
        return cellException(messageCode, null);
    }

    private RuntimeException cellException(final String messageCode, final Throwable cause)
    {
        return new AssetCellException(
                Application.messageSourceAccessor().getMessage(messageCode),
                sheet.name(),
                row.index(),
                column.index(),
                column.name(),
                column.type(),
                Objects.nonNull(cell) ? cell.getValue() : null,
                column.isNullable(),
                cause
        );
    }

    public boolean isPrimary()
    {
        return column.isPrimary();
    }

    public boolean isEmpty()
    {
        return Objects.isNull(cell) || cell.getText().isEmpty();
    }

    @Override
    public String toString()
    {
        return "{column=" + column + ", value=" + cell.getValue() + "}";
    }
}
