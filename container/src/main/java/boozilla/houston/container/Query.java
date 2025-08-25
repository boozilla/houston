package boozilla.houston.container;

import boozilla.houston.asset.AssetData;
import boozilla.houston.asset.QueryResultInfo;
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
import java.util.function.Consumer;
import java.util.stream.Collectors;

public record Query(
        Set<String> columns,
        String from,
        String where,
        long offset,
        long limit,
        String sql
) {
    private static final char SINGLE_QUOTE = '\'';
    private static final String OP_NOT_EQUAL = "<>";
    private static final String OP_EQUAL = "=";
    private static final String KW_IS = "IS";
    private static final String KW_NOT = "NOT";

    public static Query of(final String value)
    {
        try
        {
            final var parser = CCJSqlParserUtil.parse(normalizeWhereOperators(value));

            if(parser instanceof Select select)
                return of(select);
        }
        catch(JSQLParserException e)
        {
            throw Status.INVALID_ARGUMENT.withDescription(e.getLocalizedMessage())
                    .withCause(e)
                    .asRuntimeException();
        }

        throw Status.INVALID_ARGUMENT.withDescription("Support only 'select' query (%s)".formatted(value))
                .asRuntimeException();
    }

    private static Query of(final Select parser)
    {
        return of(parser.getPlainSelect());
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
        else
            sql = sql.concat(" ORDER BY code ASC");

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

    private static int skipWhitespace(final String s, int idx, final int len)
    {
        while(idx < len && Character.isWhitespace(s.charAt(idx)))
        {
            idx++;
        }

        return idx;
    }

    private static int scanLetters(final String s, int idx, final int len)
    {
        while(idx < len && Character.isLetter(s.charAt(idx)))
        {
            idx++;
        }

        return idx;
    }

    private static String normalizeWhereOperators(final String whereClause)
    {
        if(Objects.isNull(whereClause) || whereClause.isBlank())
            return whereClause;

        final var length = whereClause.length();
        final var builder = new StringBuilder(length);
        var inSingleQuote = false;

        for(var i = 0; i < length; i++)
        {
            final var ch = whereClause.charAt(i);

            // 문자열 리터럴 처리: '' 이스케이프 지원
            if(ch == SINGLE_QUOTE)
            {
                if(inSingleQuote)
                {
                    if(i + 1 < length && whereClause.charAt(i + 1) == SINGLE_QUOTE)
                    {
                        builder.append("''");
                        i++;
                        continue;
                    }
                    else
                    {
                        inSingleQuote = false;
                        builder.append(ch);
                        continue;
                    }
                }
                else
                {
                    inSingleQuote = true;
                    builder.append(ch);
                    continue;
                }
            }

            if(!inSingleQuote)
            {
                // != -> <>
                if(ch == '!' && i + 1 < length && whereClause.charAt(i + 1) == '=')
                {
                    builder.append(OP_NOT_EQUAL);
                    i++;
                    continue;
                }

                // == -> =
                if(ch == '=' && i + 1 < length && whereClause.charAt(i + 1) == '=')
                {
                    builder.append(OP_EQUAL);
                    i++;
                    continue;
                }

                // IS / IS NOT (토큰 경계에서만)
                if(Character.isLetter(ch))
                {
                    final var start = i;
                    var j = scanLetters(whereClause, i, length);
                    final var word1 = whereClause.substring(start, j);

                    if(word1.equalsIgnoreCase(KW_IS))
                    {
                        final var leftBoundary = start == 0 || !Character.isLetterOrDigit(whereClause.charAt(start - 1));
                        final var rightBoundaryAfterIS = j == length || !Character.isLetterOrDigit(whereClause.charAt(j));
                        if(leftBoundary)
                        {
                            // 공백 스킵
                            var k = skipWhitespace(whereClause, j, length);

                            // IS NOT 인지 확인
                            if(k < length && Character.isLetter(whereClause.charAt(k)))
                            {
                                final var n = scanLetters(whereClause, k, length);
                                final var word2 = whereClause.substring(k, n);
                                final var rightBoundaryAfterNOT = n == length || !Character.isLetterOrDigit(whereClause.charAt(n));

                                if(word2.equalsIgnoreCase(KW_NOT) && rightBoundaryAfterNOT)
                                {
                                    builder.append(OP_NOT_EQUAL);
                                    i = n - 1;
                                    continue;
                                }
                            }

                            // 단독 IS 인 경우: = 로 정규화
                            if(rightBoundaryAfterIS)
                            {
                                builder.append(OP_EQUAL);
                                i = j - 1;
                                continue;
                            }
                        }
                    }

                    // 다른 식별자/단어는 그대로 출력
                    builder.append(whereClause, start, j);
                    i = j - 1;
                    continue;
                }
            }

            // 기본 문자 출력
            builder.append(ch);
        }

        return builder.toString();
    }

    public boolean allColumns()
    {
        return columns.isEmpty();
    }

    public Flux<AssetData> result(final AssetQuery assetQuery,
                                  final List<Descriptors.FieldDescriptor> fieldDescriptor,
                                  final Consumer<QueryResultInfo> resultInfoConsumer)
    {
        return Flux.using(() -> assetQuery.query(sql(), resultInfoConsumer),
                        result -> Flux.fromStream(() -> {
                                    var stream = result.stream();

                                    if(offset() > 0)
                                        stream = stream.skip(offset());

                                    if(limit() > 0)
                                        stream = stream.limit(limit());

                                    return stream;
                                })
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
