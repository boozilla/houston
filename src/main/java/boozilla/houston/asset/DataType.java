package boozilla.houston.asset;

import org.dhatim.fastexcel.reader.Cell;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.function.Function;

public enum DataType {
    UNKNOWN,
    LONG(Set.of("long", "int64"),
            cell -> (long) Double.parseDouble(getText(cell).trim()),
            obj -> Long.parseLong(obj.toString()),
            Long.class),
    INTEGER(Set.of("integer", "int", "int32"),
            cell -> (int) Double.parseDouble(getText(cell).trim()),
            obj -> Integer.parseInt(obj.toString()),
            Integer.class),
    DOUBLE(Set.of("double", "float"),
            cell -> Double.parseDouble(getText(cell).trim()),
            obj -> Double.parseDouble(obj.toString()),
            Double.class),
    STRING(Set.of("string", "str"),
            DataType::getText,
            Object::toString,
            String.class),
    BOOLEAN(Set.of("boolean", "bool"),
            cell -> Boolean.parseBoolean(getText(cell).trim()),
            obj -> Boolean.parseBoolean(obj.toString()),
            Boolean.class),
    DATE(Set.of("date", "datetime", "timezone", "timestamp"),
            cell -> ZonedDateTime.parse(cell.getText().trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"))
                    .toInstant()
                    .toEpochMilli(),
            obj -> ZonedDateTime.parse(obj.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"))
                    .toInstant()
                    .toEpochMilli(),
            Long.class);

    private final Set<String> symbols;
    private final Function<Cell, Object> extractor;
    private final Function<Object, Object> cast;
    private final Class<?> typeClass;

    DataType()
    {
        this.symbols = Set.of();
        this.extractor = cell -> null;
        this.cast = value -> null;
        this.typeClass = null;
    }

    DataType(final Set<String> symbols, final Function<Cell, Object> extractor, final Function<Object, Object> cast, final Class<?> typeClass)
    {
        this.symbols = symbols;
        this.extractor = extractor;
        this.cast = cast;
        this.typeClass = typeClass;
    }

    private static String getText(final Cell cell)
    {
        return switch(cell.getType())
        {
            case STRING, FORMULA -> cell.getText();
            case NUMBER -> cell.asNumber().toString();
            case BOOLEAN -> cell.asBoolean().toString();
            default -> throw new IllegalStateException("Unexpected value: " + cell.getType());
        };
    }

    public static DataType from(final String symbol)
    {
        for(final var type : DataType.values())
        {
            if(type.symbols.contains(symbol.toLowerCase()))
                return type;
        }

        return UNKNOWN;
    }

    public Object extract(final Cell cell)
    {
        return extractor.apply(cell);
    }

    public Object cast(final Object value)
    {
        return cast.apply(value);
    }

    public Class<?> typeClass()
    {
        return this.typeClass;
    }
}
