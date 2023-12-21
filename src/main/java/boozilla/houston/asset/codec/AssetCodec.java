package boozilla.houston.asset.codec;

import boozilla.houston.asset.AssetSheet;

import java.util.List;

public interface AssetCodec<T, R> {
    T serialize(final AssetSheet sheet);

    List<R> deserialize(final T data);
}
