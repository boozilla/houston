package boozilla.houston.asset;

import boozilla.houston.Application;
import boozilla.houston.exception.AssetColumnException;
import boozilla.houston.exception.AssetSheetException;
import org.dhatim.fastexcel.reader.Cell;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class AssetSheet implements AutoCloseable {
    // Column 은 0 부터 시작
    private static final int START_OF_COLUMN = 1;
    private final RowPosition rowPosition;
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
        this.rowPosition = new RowPosition(openStream());

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

        try(final var rows = openStream().skip(rowPosition.startOfData))
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
            final var nameRow = rows.filter(row -> row.getRowNum() == rowPosition.headers.get(RowPosition.Category.NAME)
                            .getRowNum())
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

        final var columnBuilders = new HashMap<Integer, AssetColumn.AssetColumnBuilder>();

        rowPosition.headers.forEach((category, row) -> {
            switch(category)
            {
                case COMMENT ->
                        consumeColumn(row, columnBuilders, (builder, cell) -> builder.comment(((String) DataType.STRING.extract(cell)).trim()));
                case NAME ->
                        consumeColumn(row, columnBuilders, (builder, cell) -> builder.name(((String) DataType.STRING.extract(cell)).trim()));
                case TYPE ->
                        consumeColumn(row, columnBuilders, (builder, cell) -> builder.type(((String) DataType.STRING.extract(cell)).trim()));
                case SCOPE ->
                        consumeColumn(row, columnBuilders, (builder, cell) -> builder.scope(((String) DataType.STRING.extract(cell)).trim()));
                case NULLABLE ->
                        consumeColumn(row, columnBuilders, (builder, cell) -> builder.nullable((boolean) DataType.BOOLEAN.extract(cell)), builder -> builder.nullable(false));
                case LINK ->
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

    private void putColumn(final Map<Integer, AssetColumn> container, final Integer index, final AssetColumn column)
    {
        final var messageAccessor = Application.messageSourceAccessor().getMessage("READ_SHEET_ERROR_EMPTY_COLUMN");

        if(Objects.isNull(column.name()) || column.name().isEmpty())
        {
            final var rowNum = rowPosition.headers.get(RowPosition.Category.NAME)
                    .getRowNum();

            throw new AssetColumnException(messageAccessor.formatted("name"), sheet.getName(), rowNum, index);
        }

        if(column.type() == DataType.UNKNOWN)
        {
            final var rowNum = rowPosition.headers.get(RowPosition.Category.TYPE)
                    .getRowNum();

            throw new AssetColumnException(messageAccessor.formatted("type"), sheet.getName(), rowNum, index);
        }

        if(column.scope().isEmpty())
        {
            final var rowNum = rowPosition.headers.get(RowPosition.Category.SCOPE)
                    .getRowNum();

            throw new AssetColumnException(messageAccessor.formatted("scope"), sheet.getName(), rowNum, index);
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

    static class RowPosition {
        // Row 는 1 부터 시작
        private static final int COMMENT_HEADER_POSITION = 2;
        private final Map<Category, Row> headers;
        private final int startOfData;
        public RowPosition(final Stream<Row> rows)
        {
            headers = new HashMap<>();

            Flux.fromStream(rows)
                    .skip(COMMENT_HEADER_POSITION - 1)
                    .takeUntil(row -> foundAll())
                    .doOnNext(row -> {
                        if(row.getRowNum() == COMMENT_HEADER_POSITION)
                        {
                            headers.put(Category.COMMENT, row);
                            return;
                        }

                        findCategory(row)
                                .ifPresent(category -> headers.computeIfAbsent(category, key -> row));
                    })
                    .then()
                    .subscribeOn(Schedulers.boundedElastic())
                    .block();

            startOfData = calcStartOfData();
        }

        private Optional<Category> findCategory(final Row row)
        {
            final var cell = row.getCell(0);

            if(Objects.nonNull(cell))
            {
                final var text = cell.getText();

                for(final var category : Category.values())
                {
                    if(text.equalsIgnoreCase(category.name))
                        return Optional.of(category);
                }
            }

            return Optional.empty();
        }

        private boolean foundAll()
        {
            return Category.values().length == headers.size();
        }

        private int calcStartOfData()
        {
            return headers.values()
                    .stream()
                    .mapToInt(Row::getRowNum)
                    .max()
                    .orElseThrow();
        }

        enum Category {
            COMMENT(null),
            NAME("name"),
            TYPE("type"),
            SCOPE("scope"),
            NULLABLE("nullable"),
            LINK("link");

            private final String name;

            Category(final String name)
            {
                this.name = name;
            }
        }
    }
}
