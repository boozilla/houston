package boozilla.houston.asset.sql;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Where implements SqlStatement<From> {
    private final Map<String, Object> params;
    private String condition;
    private From from;
    private Where instance;

    private Where()
    {
        this.params = new HashMap<>();
    }

    static Where is(final From from, final String condition)
    {
        final var where = new Where();
        where.from = from;
        where.instance = where;

        return where.condition(condition);
    }

    public Where parameter(final String name, final Object value)
    {
        this.instance.params.put(name, value);
        return this.instance;
    }

    private Where condition(final String condition)
    {
        this.instance.condition = condition;
        return this.instance;
    }

    private String getCondition()
    {
        return parserCondition();
    }

    public OrderBy orderBy(final String column, final String order)
    {
        return new OrderBy(this, column, order);
    }

    public Offset limit(final int offset)
    {
        return new Offset(this, offset);
    }

    public Offset limit(final int offset, final int limit)
    {
        return new Offset(this, offset, limit);
    }

    private String parserCondition()
    {
        if(this.instance.condition == null)
            return "";

        var parsedCondition = this.instance.condition;
        for(final var entry : this.instance.params.entrySet())
        {
            String paramStr;
            if(entry.getValue() instanceof Collection<?>)
                paramStr = "(" + ((Collection<?>) entry.getValue()).stream().map(this::getValueAsParamString).collect(Collectors.joining(", ")) + ")";
            else
                paramStr = getValueAsParamString(entry.getValue());

            parsedCondition = parsedCondition.replaceAll(":" + entry.getKey(), paramStr);
        }

        return parsedCondition;
    }

    private String getValueAsParamString(Object value)
    {
        if(value instanceof Enum)
            value = ((Enum<?>) value).ordinal();
        else if(value instanceof Date)
            value = ((Date) value).getTime();

        return String.format(value instanceof String ? "'%s'" : "%s", value.toString());
    }

    @Override
    public From getParent()
    {
        return this.from;
    }

    @Override
    public String getSql()
    {
        return "WHERE " + this.instance.getCondition();
    }

    @Override
    public String toString()
    {
        return getParent().toString() + " " + getSql();
    }
}
