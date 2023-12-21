package boozilla.houston.exception;

import boozilla.houston.Application;
import com.google.protobuf.JavaType;

import java.util.Objects;

public class AssetTypeMismatchException extends RuntimeException {
    private final String fromSheetName;
    private final String fromColumnName;
    private final JavaType fromType;
    private final String toSheetName;
    private final String toColumnName;
    private final JavaType toType;

    public AssetTypeMismatchException(final String fromSheetName, final String fromColumnName, final JavaType fromType,
                                      final String toSheetName, final String toColumnName, final JavaType toType)
    {
        super(Application.messageSourceAccessor().getMessage("CONSTRAINTS_ERROR_TYPE"));

        this.fromSheetName = fromSheetName;
        this.fromColumnName = fromColumnName;
        this.fromType = fromType;
        this.toSheetName = toSheetName;
        this.toColumnName = toColumnName;
        this.toType = toType;
    }

    @Override
    public String getMessage()
    {
        final var info = Application.messageSourceAccessor().getMessage("ASSET_TYPE_MISMATCH_EXCEPTION_INFO")
                .formatted(fromSheetName, fromColumnName, fromType, toSheetName, toColumnName, toType);
        return super.getMessage() + " " + info;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(fromSheetName, fromColumnName, fromType, toSheetName, toColumnName, toType);
    }

    @Override
    public boolean equals(final Object object)
    {
        if(object instanceof AssetTypeMismatchException)
        {
            return object.hashCode() == this.hashCode();
        }

        return false;
    }
}
