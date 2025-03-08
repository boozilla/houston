package boozilla.houston.asset.codec;

import boozilla.houston.asset.AssetColumn;
import boozilla.houston.asset.AssetSheet;
import boozilla.houston.asset.DataType;
import boozilla.houston.asset.Scope;
import org.apache.logging.log4j.util.Strings;

import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProtobufSchemaSerializer implements AssetSerializer<String> {
    private static final String PARTITION_COLUMN_NAME = "partition";
    private static final String PROTOBUF_TEMPLATE = """
            syntax = "proto3";
            package %s;
            option java_multiple_files = true;
            %s
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
        final var dependencies = new HashSet<String>();
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
                    final var optional = !column.array() && column.isNullable();
                    final var optionalString = optional ? "optional " : "";
                    final var repeated = column.array() ? "repeated " : "";
                    final var type = protoType(optional, column);
                    final var comment = Optional.ofNullable(column.comment())
                            .map("""
                                    
                                    \t// %s
                                    \t"""::formatted)
                            .orElse(Strings.EMPTY);

                    fieldString.append("""
                                %s%s%s%s %s = %d;
                            """.formatted(comment, optionalString, repeated, type, column.name(), column.index()));

                    if(optional)
                    {
                        dependencies.add("google/protobuf/wrappers.proto");
                    }
                });

        final var dependencyString = dependencies.stream()
                .map("\nimport \"%s\";\n"::formatted)
                .collect(Collectors.joining("\n"));

        return PROTOBUF_TEMPLATE.formatted(protoPackage, dependencyString, sheet.sheetName(), fieldString.toString());
    }

    private String protoType(final boolean optional, final AssetColumn column)
    {
        return switch(column.type())
        {
            case LONG, DATE -> optional ? "google.protobuf.Int64Value" : "int64";
            case INTEGER -> optional ? "google.protobuf.Int32Value" : "int32";
            case DOUBLE -> optional ? "google.protobuf.DoubleValue" : "double";
            case STRING -> optional ? "google.protobuf.StringValue" : "string";
            case BOOLEAN -> optional ? "google.protobuf.BoolValue" : "bool";
            default -> throw new RuntimeException("Unknown type");
        };
    }
}
