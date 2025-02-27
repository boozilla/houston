package boozilla.houston.asset.codec;

import boozilla.houston.asset.AssetColumn;
import boozilla.houston.asset.AssetSheet;
import boozilla.houston.asset.DataType;
import boozilla.houston.asset.Scope;
import org.apache.logging.log4j.util.Strings;

import java.util.Optional;
import java.util.stream.Stream;

public class ProtobufSchemaSerializer implements AssetSerializer<String> {
    private static final String PARTITION_COLUMN_NAME = "partition";
    private static final String PROTOBUF_TEMPLATE = """
            syntax = "proto3";
            package %s;
            option java_multiple_files = true;
            
            message %s {%s}
            """;

    private final String protoPackage;
    private final Scope scope;

    public ProtobufSchemaSerializer(final String protoPackage, final Scope scope)
    {
        this.protoPackage = protoPackage;
        this.scope = scope;
    }

    @Override
    public String serialize(final AssetSheet sheet)
    {
        final var fieldString = new StringBuilder();

        final var partitionName = sheet.partitionName();
        final var partitionColumn = partitionName.map(name -> AssetColumn.builder()
                        .name(PARTITION_COLUMN_NAME)
                        .type(DataType.STRING)
                        .index(sheet.endOfColumn() + 1)
                        .build())
                .stream();

        Stream.concat(sheet.columns(scope), partitionColumn)
                .forEach(column -> {
                    final var optional = !column.array() && column.isNullable() ? "optional " : "";
                    final var repeated = column.array() ? "repeated " : "";
                    final var type = protoType(column);
                    final var comment = Optional.ofNullable(column.comment())
                            .map("""
                                    
                                    \t// %s
                                    \t"""::formatted)
                            .orElse(Strings.EMPTY);

                    fieldString.append("""
                                %s%s%s%s %s = %d;
                            """.formatted(comment, optional, repeated, type, column.name(), column.index()));
                });

        return PROTOBUF_TEMPLATE.formatted(protoPackage, sheet.sheetName(), fieldString.toString());
    }

    private String protoType(final AssetColumn column)
    {
        return switch(column.type())
        {
            case LONG, DATE -> "int64";
            case INTEGER -> "int32";
            case DOUBLE -> "double";
            case STRING -> "string";
            case BOOLEAN -> "bool";
            default -> throw new RuntimeException("Unknown type");
        };
    }
}
