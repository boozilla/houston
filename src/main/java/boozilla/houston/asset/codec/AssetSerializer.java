package boozilla.houston.asset.codec;

import boozilla.houston.asset.AssetSheet;

import java.util.List;

public interface AssetSerializer<T> extends AssetCodec<T, Void> {
    T serialize(final AssetSheet sheet);

    default List<Void> deserialize(final T data)
    {
        throw new UnsupportedOperationException();
    }
}
