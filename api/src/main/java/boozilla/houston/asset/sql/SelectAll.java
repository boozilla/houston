package boozilla.houston.asset.sql;

import com.google.protobuf.GeneratedMessageV3;

public interface SelectAll {
    From from(final String sheet);

    From from(final Class<? extends GeneratedMessageV3> sheet);
}
