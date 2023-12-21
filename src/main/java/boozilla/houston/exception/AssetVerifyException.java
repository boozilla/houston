package boozilla.houston.exception;

import boozilla.houston.Application;

import java.util.Objects;

public class AssetVerifyException extends RuntimeException {
    private final String sheetName;
    private final String columnName;
    private final Object value;

    public AssetVerifyException(final String message, final String sheetName, final String columnName, final Object value)
    {
        super(Application.messageSourceAccessor().getMessage(message));
        this.sheetName = sheetName;
        this.columnName = columnName;
        this.value = value;
    }

    @Override
    public String getMessage()
    {
        final var info = Application.messageSourceAccessor().getMessage("ASSET_VERIFY_EXCEPTION_INFO")
                .formatted(sheetName, columnName, value);
        return super.getMessage() + " " + info;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(sheetName, columnName, value);
    }

    @Override
    public boolean equals(final Object object)
    {
        if(object instanceof AssetVerifyException)
        {
            return object.hashCode() == this.hashCode();
        }

        return false;
    }
}
