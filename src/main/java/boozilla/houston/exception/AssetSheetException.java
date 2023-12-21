package boozilla.houston.exception;

import boozilla.houston.Application;

public class AssetSheetException extends RuntimeException {
    private final String sheetName;

    public AssetSheetException(final String message, final String sheetName, final Throwable cause)
    {
        super(message, cause);
        this.sheetName = sheetName;
    }

    public String sheetName()
    {
        return this.sheetName;
    }

    @Override
    public String getMessage()
    {
        final var info = Application.messageSourceAccessor().getMessage("ASSET_SHEET_EXCEPTION_INFO")
                .formatted(sheetName());
        return super.getMessage() + " " + info;
    }
}
