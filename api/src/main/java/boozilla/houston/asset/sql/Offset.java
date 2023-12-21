package boozilla.houston.asset.sql;

public class Offset implements SqlStatement<SqlStatement<?>> {
    private final SqlStatement<?> parent;

    private final int offset;
    private final int limit;

    public Offset(final SqlStatement<?> parent, final int offset)
    {
        this(parent, offset, 0);
    }

    public Offset(final SqlStatement<?> parent, final int offset, final int limit)
    {
        this.parent = parent;
        this.offset = offset;
        this.limit = limit;
    }

    @Override
    public Offset offset()
    {
        return this;
    }

    @Override
    public SqlStatement<?> getParent()
    {
        return this.parent;
    }

    @Override
    public String getSql()
    {
        return "LIMIT " + this.offset + (this.limit > 0 ? ", " + this.limit : "");
    }

    public String toString()
    {
        return getParent().toString() + " " + getSql();
    }
}
