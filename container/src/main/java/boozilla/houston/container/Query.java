package boozilla.houston.container;

import boozilla.houston.asset.AssetData;
import com.google.protobuf.Descriptors;
import com.googlecode.cqengine.query.parser.common.InvalidQueryException;
import com.googlecode.cqengine.resultset.ResultSet;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public record Query(
        Set<String> columns,
        String from,
        String where,
        long offset,
        long limit,
        String sql
) {
    public static Query of(final String value)
    {
        try
        {
            final var parser = CCJSqlParserUtil.parse(value);

            if(parser instanceof Select select)
                return of(select);
        }
        catch(JSQLParserException e)
        {
            throw new RuntimeException(value, e);
        }

        throw new RuntimeException("Support only 'select' query (%s)".formatted(value));
    }

    private static Query of(final Select parser)
    {
        return of(parser.getSelectBody(PlainSelect.class));
    }

    private static Query of(final PlainSelect parser)
    {
        final var columns = parser.getSelectItems().stream()
                .map(Object::toString)
                .collect(Collectors.toUnmodifiableSet());
        final var from = parser.getFromItem().toString();
        final var where = whereQuote(parser.getWhere());
        final var orderBy = Objects.isNull(parser.getOrderByElements()) ? "" :
                parser.getOrderByElements().stream()
                        .map(item -> {
                            final var columnName = item.getExpression().toString().replaceAll("\\b(\\w+)\\b", "'$1'");
                            final var order = item.isAsc() ? "ASC" : "DESC";

                            return "%s %s".formatted(columnName, order);
                        })
                        .collect(Collectors.joining(", "));
        final var groupBy = parser.getGroupBy();
        final var limit = parser.getLimit();
        final var offsetValue = Objects.isNull(limit) ? 0L : Objects.isNull(limit.getOffset()) ? 0L : ((LongValue) limit.getOffset()).getValue();
        final var limitValue = Objects.isNull(limit) ? 0L : Objects.isNull(limit.getRowCount()) ? 0L : ((LongValue) limit.getRowCount()).getValue();

        var sql = "SELECT * FROM %s".formatted(from);

        if(!where.isEmpty())
            sql = sql.concat(" WHERE (%s)".formatted(where));

        if(!orderBy.isEmpty())
            sql = sql.concat(" ORDER BY %s".formatted(orderBy));

        if(Objects.nonNull(groupBy))
            throw new RuntimeException("GROUP BY is not supported");

        return new Query(columns.contains("*") ? Set.of() : columns,
                from, where, offsetValue, limitValue, sql);
    }

    private static String whereQuote(final Expression expression)
    {
        if(Objects.isNull(expression))
            return "";

        if(expression instanceof BinaryExpression binary)
        {
            final var left = binary.getLeftExpression();
            final var right = binary.getRightExpression();
            final var operator = binary.getStringExpression();

            final var leftValue = whereQuote(left);
            final var rightValue = whereQuote(right);

            return "%s %s %s".formatted(leftValue, operator, rightValue);
        }
        else if(expression instanceof Column column)
            return column.getColumnName().replaceAll("\\b(\\w+)\\b", "'$1'");

        return expression.toString();
    }

    public boolean allColumns()
    {
        return columns.isEmpty();
    }

    public Flux<AssetData> result(final AssetQuery assetQuery, final List<Descriptors.FieldDescriptor> fieldDescriptor)
    {
        return Flux.using(() -> assetQuery.query(sql()),
                        result -> Flux.fromStream(result.stream())
                                .parallel()
                                .map(message -> {
                                    if(allColumns())
                                        return new AssetData(message, fieldDescriptor);

                                    final var builder = message.toBuilder();

                                    message.getAllFields().entrySet().stream()
                                            .filter(entry -> !columns().contains(entry.getKey().getName()))
                                            .forEach(entry -> builder.clearField(entry.getKey()));

                                    return new AssetData(builder, fieldDescriptor);
                                }),
                        ResultSet::close)
                .onErrorMap(InvalidQueryException.class, error -> new StatusRuntimeException(Status.ABORTED
                        .withDescription(error.getMessage())
                        .withCause(error)));
    }
}
