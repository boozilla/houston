package boozilla.houston.asset.sql;

import com.google.protobuf.AbstractMessage;

public interface SelectAll {
    From from(final String sheet);

    From from(final Class<? extends AbstractMessage> sheet);
}
