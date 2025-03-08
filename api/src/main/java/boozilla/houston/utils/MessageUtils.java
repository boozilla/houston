package boozilla.houston.utils;

import com.google.protobuf.DynamicMessage;

import java.util.Optional;

public class MessageUtils {
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> extractValue(final Object row)
    {
        final Optional<T> value;

        if(row instanceof final DynamicMessage message)
        {
            value = (Optional<T>) message.getAllFields()
                    .values()
                    .stream()
                    .findAny();
        }
        else
        {
            value = (Optional<T>) Optional.ofNullable(row);
        }

        return value;
    }
}
