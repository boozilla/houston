package boozilla.houston.exception;

import boozilla.houston.Application;

public class AssetColumnException extends RuntimeException {
    private final String sheetName;
    private final int rowIndex;
    private final int columnIndex;

    public AssetColumnException(final String message, final String sheetName, final int rowIndex, final int columnIndex)
    {
        this(message, sheetName, rowIndex, columnIndex, null);
    }

    public AssetColumnException(final String message, final String sheetName, final int rowIndex, final int columnIndex,
                                final Throwable cause)
    {
        super(message, cause);
        this.sheetName = sheetName;
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
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

    public String address()
    {
        return "%c%d".formatted((char) (columnIndex() + 'A'), rowIndex());
    }

    @Override
    public String getMessage()
    {
        final var info = Application.messageSourceAccessor().getMessage("ASSET_COLUMN_EXCEPTION_INFO")
                .formatted(sheetName(), address());
        return super.getMessage() + " " + info;
    }
}
