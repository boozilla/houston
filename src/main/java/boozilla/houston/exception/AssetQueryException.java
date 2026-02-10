package boozilla.houston.exception;

import boozilla.houston.Application;

public class AssetQueryException extends RuntimeException {
    private final String tableName;

    public AssetQueryException(final String message, final String tableName)
    {
        super(Application.messageSourceAccessor().getMessage(message, new Object[]{tableName}));
        this.tableName = tableName;
    }

    public String tableName()
    {
        return this.tableName;
    }
}
