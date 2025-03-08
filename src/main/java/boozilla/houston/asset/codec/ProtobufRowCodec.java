package boozilla.houston.asset.codec;

import boozilla.houston.Application;
import boozilla.houston.asset.AssetColumn;
import boozilla.houston.asset.AssetSheet;
import boozilla.houston.asset.DataType;
import boozilla.houston.asset.Scope;
import boozilla.houston.exception.AssetSheetException;
import com.google.protobuf.*;
import org.springframework.util.FastByteArrayOutputStream;

import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ProtobufRowCodec implements AssetCodec<byte[], DynamicMessage> {
    private final static String PARTITION_FIELD_NAME = "partition";

    private Supplier<DynamicMessage.Builder> builder;
    private Descriptors.FileDescriptor fileDescriptor;
    private List<Descriptors.FieldDescriptor> fieldDescriptors;

    public ProtobufRowCodec(final String protoPackage, final AssetSheet sheet, final Scope scope)
    {
        final var dependencies = new HashSet<String>();
        final var messageTypes = new HashMap<String, DescriptorProtos.DescriptorProto>();
        final var fields = sheet.columns(scope)
                .peek(column -> {
                    if(column.isNullable())
                    {
                        dependencies.add("google/protobuf/wrappers.proto");

                        final var typeMessage = wrapperDescriptor(column.type());
                        messageTypes.put(typeMessage.getFullName(), typeMessage.toProto());
                    }
                })
                .map(this::buildField)
                .toList();

        final var sheetName = sheet.sheetName();
        final var descriptorProto = DescriptorProtos.DescriptorProto.newBuilder()
                .setName(sheetName)
                .addAllField(fields);

        final var partitionName = sheet.partitionName();
        if(partitionName.isPresent())
        {
            final var partitionField = buildField(AssetColumn.builder()
                    .name(PARTITION_FIELD_NAME)
                    .type(DataType.STRING)
                    .index(descriptorProto.getFieldCount() + 1)
                    .build());

            descriptorProto.addField(partitionField);
        }

        final var fileDescriptorProto = DescriptorProtos.FileDescriptorProto.newBuilder()
                .setName(sheetName)
                .setSyntax("proto3")
                .setPackage(protoPackage)
                .addAllDependency(dependencies)
                .addAllMessageType(messageTypes.values())
                .setOptions(DescriptorProtos.FileOptions.newBuilder()
                        .setJavaMultipleFiles(true))
                .addMessageType(descriptorProto);

        try
        {
            final var fileDescriptor = Descriptors.FileDescriptor.buildFrom(fileDescriptorProto.build(), new Descriptors.FileDescriptor[0]);

            initialize(sheetName, fileDescriptor);
        }
        catch(Descriptors.DescriptorValidationException e)
        {
            throw new RuntimeException(e);
        }
    }

    public ProtobufRowCodec(final String name, final Descriptors.FileDescriptor fileDescriptor)
    {
        initialize(name, fileDescriptor);
    }

    private void initialize(final String name, final Descriptors.FileDescriptor fileDescriptor)
    {
        this.fileDescriptor = fileDescriptor;

        final var messageDescriptor = fileDescriptor.findMessageTypeByName(name);

        this.fieldDescriptors = messageDescriptor.getFields();
        this.builder = () -> DynamicMessage.newBuilder(messageDescriptor);
    }

    private DescriptorProtos.FieldDescriptorProto buildField(final AssetColumn column)
    {
        final var builder = DescriptorProtos.FieldDescriptorProto.newBuilder();
        final var label = column.array() ? DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED : null;

        if(Objects.nonNull(label))
            builder.setLabel(label);

        if(column.isNullable() && !column.array())
        {
            builder.setType(DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE)
                    .setTypeName(protoTypeName(column));
        }
        else
        {
            builder.setType(protoType(column));
        }

        return builder.setName(column.name())
                .setNumber(column.index())
                .build();
    }

    private DescriptorProtos.FieldDescriptorProto.Type protoType(final AssetColumn column)
    {
        return switch(column.type())
        {
            case LONG, DATE -> DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64;
            case INTEGER -> DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32;
            case DOUBLE -> DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE;
            case STRING -> DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING;
            case BOOLEAN -> DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL;
            default -> throw new RuntimeException("Unknown type");
        };
    }

    private String protoTypeName(final AssetColumn column)
    {
        return wrapperDescriptor(column.type()).getName();
    }

    private Descriptors.Descriptor wrapperDescriptor(final DataType type)
    {
        return switch(type)
        {
            case LONG, DATE -> Int64Value.getDescriptor();
            case INTEGER -> Int32Value.getDescriptor();
            case DOUBLE -> DoubleValue.getDescriptor();
            case STRING -> StringValue.getDescriptor();
            case BOOLEAN -> BoolValue.getDescriptor();
            default -> throw new RuntimeException("Unknown type");
        };
    }

    private Object extractValue(final AssetColumn column, final Object value)
    {
        if(!column.isNullable() || column.array())
            return value;

        final var wrapperType = wrapperDescriptor(column.type());
        final var builder = DynamicMessage.newBuilder(wrapperType);

        if(Objects.isNull(value))
            return null;

        return builder.setField(wrapperType.findFieldByName("value"), value)
                .build();
    }

    public Descriptors.FileDescriptor getFileDescriptor()
    {
        return fileDescriptor;
    }

    public List<Descriptors.FieldDescriptor> getFieldDescriptor()
    {
        return fieldDescriptors;
    }

    @Override
    public byte[] serialize(final AssetSheet sheet)
    {
        try(final var output = new FastByteArrayOutputStream())
        {
            final var fieldDescriptors = this.fieldDescriptors.stream()
                    .collect(Collectors.toUnmodifiableMap(
                            Descriptors.FieldDescriptor::getName,
                            descriptor -> descriptor
                    ));

            sheet.rows(row -> {
                final var builder = this.builder.get();

                sheet.columnIndices().forEach(index -> {
                    final var column = sheet.column(index);

                    if(fieldDescriptors.containsKey(column.name()))
                    {
                        final var value = extractValue(column, row.value(index));

                        if(Objects.nonNull(value))
                        {
                            final var fieldDescriptor = fieldDescriptors.get(column.name());
                            builder.setField(fieldDescriptor, value);
                        }
                    }
                });

                final var partitionName = sheet.partitionName();
                partitionName.ifPresent(name -> builder.setField(fieldDescriptors.get(PARTITION_FIELD_NAME), name));

                try
                {
                    builder.build().writeDelimitedTo(output);
                }
                catch(Exception e)
                {
                    throw new AssetSheetException(
                            Application.messageSourceAccessor().getMessage("READ_SHEET_ERROR_FAILED_SERIALIZE"),
                            sheet.name(), e);
                }
            });

            return output.toByteArray();
        }
    }

    @Override
    public List<DynamicMessage> deserialize(final byte[] data)
    {
        final var result = new ArrayList<DynamicMessage>();
        final var input = CodedInputStream.newInstance(data);

        try
        {
            while(!input.isAtEnd())
            {
                final var builder = this.builder.get();
                input.readMessage(builder, ExtensionRegistry.getEmptyRegistry());

                result.add(builder.build());
            }
        }
        catch(IOException e)
        {
            throw new RuntimeException("Failed to deserialize asset data", e);
        }

        return result;
    }
}
