package boozilla.houston.asset;

import com.google.protobuf.*;
import com.google.protobuf.util.JsonFormat;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class AssetData {
    private final DynamicMessage message;
    private final Map<String, Descriptors.FieldDescriptor> fieldDescriptors;

    public AssetData(final DynamicMessage.Builder builder, final List<Descriptors.FieldDescriptor> fieldDescriptors)
    {
        this(builder.build(), fieldDescriptors);
    }

    public AssetData(final DynamicMessage message, final List<Descriptors.FieldDescriptor> fieldDescriptors)
    {
        this.message = message;
        this.fieldDescriptors = fieldDescriptors.stream().collect(Collectors.toUnmodifiableMap(
                Descriptors.FieldDescriptor::getName,
                Function.identity()
        ));
    }

    public Any any()
    {
        return Any.pack(this.message);
    }

    public <T extends GeneratedMessageV3> T message(final Class<T> tClass)
    {
        try
        {
            return any().unpack(tClass);
        }
        catch(InvalidProtocolBufferException e)
        {
            throw new RuntimeException("Message type conversion error", e);
        }
    }

    public <T> T value(final String name, final Class<T> tClass)
    {
        final var fieldDescriptor = fieldDescriptors.get(name);

        if(Objects.isNull(fieldDescriptor))
        {
            return null;
        }

        return tClass.cast(message.getField(fieldDescriptor));
    }

    public <T> List<T> list(final String name, final Class<T> tClass)
    {
        final var fieldDescriptor = fieldDescriptors.get(name);

        if(Objects.isNull(fieldDescriptor))
        {
            return List.of();
        }

        return IntStream.range(0, message.getRepeatedFieldCount(fieldDescriptor))
                .mapToObj(i -> message.getRepeatedField(fieldDescriptor, i))
                .map(tClass::cast)
                .toList();
    }

    public <T> Stream<T> stream(final String name, final Class<T> tClass)
    {
        final var fieldDescriptor = fieldDescriptors.get(name);

        if(Objects.isNull(fieldDescriptor))
        {
            return Stream.empty();
        }

        if(fieldDescriptor.isRepeated())
        {
            return list(name, tClass).stream();
        }

        return Stream.of(value(name, tClass));
    }

    public String toJsonString()
    {
        try
        {
            return JsonFormat.printer().print(this.message);
        }
        catch(InvalidProtocolBufferException e)
        {
            throw new RuntimeException("Failed to convert Asset data to JSON format", e);
        }
    }

    public byte[] toByteArray()
    {
        return this.message.toByteArray();
    }

    @Override
    public String toString()
    {
        return toJsonString();
    }
}
