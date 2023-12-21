package boozilla.houston.exception;

import boozilla.houston.Application;
import boozilla.houston.asset.DataType;

public class AssetCellException extends RuntimeException {
    private final String sheetName;
    private final int rowIndex;
    private final int columnIndex;
    private final String name;
    private final DataType type;
    private final Object value;
    private final boolean nullable;

    public AssetCellException(final String message, final String sheetName, final int rowIndex, final int columnIndex,
                              final String name, final DataType type, final Object value, final boolean nullable)
    {
        this(message, sheetName, rowIndex, columnIndex, name, type, value, nullable, null);
    }

    public AssetCellException(final String message, final String sheetName, final int rowIndex, final int columnIndex,
                              final String name, final DataType type, final Object value, final boolean nullable,
                              final Throwable cause)
    {
        super(message, cause);
        this.sheetName = sheetName;
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
        this.name = name;
        this.type = type;
        this.value = value;
        this.nullable = nullable;
    }

    public String sheetName()
    {
        return this.sheetName;
    }

    public int rowIndex()
    {
        return this.rowIndex;
    }

    public int columnIndex()
    {
        return this.columnIndex;
    }

    public String name()
    {
        return this.name;
    }

    public DataType type()
    {
        return this.type;
    }

    public boolean nullable()
    {
        return this.nullable;
    }

    public Object value()
    {
        return this.value;
    }

    public String address()
    {
        return "%c%d".formatted((char) (columnIndex() + 'A'), rowIndex());
    }

    @Override
    public String getMessage()
    {
        final var info = Application.messageSourceAccessor().getMessage("ASSET_CELL_EXCEPTION_INFO")
                .formatted(sheetName(), address(), name(), type(), nullable(), value());
        return super.getMessage() + " " + info;
    }
}
