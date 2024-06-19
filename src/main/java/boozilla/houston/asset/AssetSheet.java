package boozilla.houston.asset;

import boozilla.houston.Application;
import boozilla.houston.exception.AssetColumnException;
import boozilla.houston.exception.AssetSheetException;
import org.dhatim.fastexcel.reader.Cell;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class AssetSheet implements AutoCloseable {
    // Row 는 1 부터 시작
    private static final int ROW_COLUMN_COMMENT = 2;
    private static final int ROW_COLUMN_NAME = 3;
    private static final int ROW_COLUMN_TYPE = 4;
    private static final int ROW_COLUMN_SCOPE = 5;
    private static final int ROW_COLUMN_NULLABLE = 6;
    private static final int ROW_COLUMN_LINK = 7;
    private static final int START_OF_DATA_ROW = 7;

    // Column 은 0 부터 시작
    private static final int START_OF_COLUMN = 1;

    private final Sheet sheet;
    private final List<Throwable> analyzeExceptions;
    private final List<Throwable> readExceptions;

    private IntSummaryStatistics columnStatistics;
    private Map<Integer, AssetColumn> columns;
    private Stream<Row> rowStream;

    public AssetSheet(final Sheet sheet)
    {
        this.sheet = sheet;
        this.analyzeExceptions = new ArrayList<>();
        this.readExceptions = new ArrayList<>();

        analyzeColumns();
    }

    public String name()
    {
        return sheet.getName();
    }

    public String sheetName()
    {
        return sheet.getName().split("#", 2)[0];
    }

    public Optional<String> partitionName()
    {
        final var split = sheet.getName().split("#", 2);

        if(split.length == 2)
            return Optional.of(split[1]);
        else
            return Optional.empty();
    }

    public List<Throwable> exceptions()
    {
        return Stream.concat(analyzeExceptions.stream(), readExceptions.stream())
                .toList();
    }

    public void rows(final Consumer<AssetRow> consumer)
    {
        readExceptions.clear();

        try(final var rows = openStream().skip(START_OF_DATA_ROW))
        {
            rows.map(row -> new AssetRow(this, row))
                    .takeWhile(row -> !row.endOfRow())
                    .forEach(row -> {
                        try
                        {
                            consumer.accept(row);
                        }
                        catch(Exception e)
                        {
                            readExceptions.add(e);
                        }
                    });
        }
        catch(Exception e)
        {
            readExceptions.add(e);
        }
    }

    public AssetColumn column(final Cell cell)
    {
        return column(cell.getColumnIndex());
    }

    public AssetColumn column(final int index)
    {
        return columns.get(index);
    }

    public IntStream columnIndices()
    {
        return IntStream.rangeClosed(startOfColumn(), endOfColumn());
    }

    public Stream<AssetColumn> columns(final Scope scope)
    {
        return columnIndices()
                .mapToObj(this::column)
                .filter(column -> column.scope().contains(scope));
    }

    public int endOfColumn()
    {
        return columnStatistics.getMax();
    }

    public int startOfColumn()
    {
        return columnStatistics.getMin();
    }

    private void analyzeColumns()
    {
        try(final var rows = openStream())
        {
            final var nameRow = rows.filter(row -> row.getRowNum() == ROW_COLUMN_NAME)
                    .findAny()
                    .orElseThrow();

            columnStatistics = nameRow.stream()
                    .skip(START_OF_COLUMN)
                    .filter(cell -> Objects.nonNull(cell) && !cell.getText().isEmpty())
                    .mapToInt(Cell::getColumnIndex)
                    .summaryStatistics();
        }
        catch(Exception e)
        {
            analyzeExceptions.add(e);
        }

        try(final var rows = openStream())
        {
            final var columnBuilders = new HashMap<Integer, AssetColumn.AssetColumnBuilder>();

            rows.limit(START_OF_DATA_ROW)
                    .forEach(row -> {
                        switch(row.getRowNum())
                        {
                            case ROW_COLUMN_COMMENT ->
                                    consumeColumn(row, columnBuilders, (builder, cell) -> builder.comment(((String) DataType.STRING.extract(cell)).trim()));
                            case ROW_COLUMN_NAME ->
                                    consumeColumn(row, columnBuilders, (builder, cell) -> builder.name(((String) DataType.STRING.extract(cell)).trim()));
                            case ROW_COLUMN_TYPE ->
                                    consumeColumn(row, columnBuilders, (builder, cell) -> builder.type(((String) DataType.STRING.extract(cell)).trim()));
                            case ROW_COLUMN_SCOPE ->
                                    consumeColumn(row, columnBuilders, (builder, cell) -> builder.scope(((String) DataType.STRING.extract(cell)).trim()));
                            case ROW_COLUMN_NULLABLE ->
                                    consumeColumn(row, columnBuilders, (builder, cell) -> builder.nullable((boolean) DataType.BOOLEAN.extract(cell)), builder -> builder.nullable(false));
                            case ROW_COLUMN_LINK ->
                                    consumeColumn(row, columnBuilders, (builder, cell) -> builder.link(((String) DataType.STRING.extract(cell)).trim()), builder -> builder.link(null));
                        }
                    });

            columns = columnBuilders.entrySet().stream()
                    .reduce(new HashMap<>(), (map, entry) -> {
                        final var index = entry.getKey();
                        final var column = entry.getValue().build();

                        putColumn(map, index, column);

                        return map;
                    }, (x, y) -> {
                        x.putAll(y);

                        return x;
                    });
        }
        catch(Exception e)
        {
            analyzeExceptions.add(e);
        }
    }

    private void putColumn(final Map<Integer, AssetColumn> container, final Integer index, final AssetColumn column)
    {
        final var messageAccessor = Application.messageSourceAccessor().getMessage("READ_SHEET_ERROR_EMPTY_COLUMN");

        if(Objects.isNull(column.name()) || column.name().isEmpty())
        {
            throw new AssetColumnException(messageAccessor.formatted("name"), sheet.getName(), ROW_COLUMN_NAME, index);
        }

        if(column.type() == DataType.UNKNOWN)
        {
            throw new AssetColumnException(messageAccessor.formatted("type"), sheet.getName(), ROW_COLUMN_TYPE, index);
        }

        if(column.scope().isEmpty())
        {
            throw new AssetColumnException(messageAccessor.formatted("scope"), sheet.getName(), ROW_COLUMN_SCOPE, index);
        }

        container.put(index, column);
    }

    private void consumeColumn(final Row row,
                               final Map<Integer, AssetColumn.AssetColumnBuilder> builders,
                               final BiConsumer<AssetColumn.AssetColumnBuilder, Cell> consumer)
    {
        consumeColumn(row, builders, consumer, null);
    }

    private void consumeColumn(final Row row,
                               final Map<Integer, AssetColumn.AssetColumnBuilder> builders,
                               final BiConsumer<AssetColumn.AssetColumnBuilder, Cell> consumer,
                               final Consumer<AssetColumn.AssetColumnBuilder> defaultConsumer)
    {
        row.stream()
                .skip(startOfColumn())
                .limit(endOfColumn())
                .takeWhile(Objects::nonNull)
                .forEach(cell -> {
                    final var builder = builders.computeIfAbsent(
                            cell.getColumnIndex(),
                            index -> AssetColumn.builder().index(index)
                    );

                    try
                    {
                        if(!cell.getText().isEmpty())
                            consumer.accept(builder, cell);
                        else if(defaultConsumer != null)
                            defaultConsumer.accept(builder);
                    }
                    catch(Exception e)
                    {
                        throw new AssetColumnException("READ_SHEET_ERROR_ILLEGAL_COLUMN", sheet.getName(), row.getRowNum(), cell.getColumnIndex(), e);
                    }
                });
    }

    private Stream<Row> openStream()
    {
        if(Objects.nonNull(rowStream))
            rowStream.close();

        try
        {
            rowStream = sheet.openStream();
            return rowStream;
        }
        catch(IOException e)
        {
            throw new AssetSheetException(
                    Application.messageSourceAccessor().getMessage("READ_SHEET_ERROR_FAILED_OPEN"),
                    sheet.getName(), e);
        }
    }

    public boolean isEmpty(final Scope scope)
    {
        return columns(scope).findAny().isEmpty();
    }

    @Override
    public void close()
    {
        if(Objects.nonNull(rowStream))
            rowStream.close();
    }
}
