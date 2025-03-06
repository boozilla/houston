package boozilla.houston.utils;

import com.google.protobuf.DynamicMessage;
import lombok.experimental.UtilityClass;

import java.util.Optional;

@UtilityClass
public class MessageUtils {
    @SuppressWarnings("unchecked")
    public <T> Optional<T> extractValue(final Object row)
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
