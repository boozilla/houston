package boozilla.houston.asset.sql;

import java.util.HashMap;
import java.util.Map;

public class OrderBy implements SqlStatement<Where> {
    private final Where where;
    private final Map<String, String> orders;

    public OrderBy(final Where where, final String column, final String order)
    {
        this.where = where;
        this.orders = new HashMap<>();
        this.orders.put(column, order);
    }

    public OrderBy thenBy(final String column, final String order)
    {
        this.orders.put(column, order);
        return this;
    }

    public Offset limit(final int offset)
    {
        return new Offset(this, offset);
    }

    public Offset limit(final int offset, final int limit)
    {
        return new Offset(this, offset, limit);
    }

    @Override
    public Where getParent()
    {
        return this.where;
    }

    @Override
    public String getSql()
    {
        return "ORDER BY " + this.orders.entrySet().stream()
                .map(entry -> entry.getKey() + " " + entry.getValue())
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }

    public String toString()
    {
        return getParent().toString() + " " + getSql();
    }
}
