package boozilla.houston.asset.sql;

import com.google.protobuf.GeneratedMessage;

public interface SelectAll {
    From from(final String sheet);

    From from(final Class<? extends GeneratedMessage> sheet);
}
