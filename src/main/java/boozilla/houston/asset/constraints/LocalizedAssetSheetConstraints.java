package boozilla.houston.asset.constraints;

import boozilla.houston.Application;

public abstract class LocalizedAssetSheetConstraints implements AssetSheetConstraints {
    protected String message(final String code, final Object... args)
    {
        return Application.messageSourceAccessor().getMessage(code).formatted(args);
    }
}
