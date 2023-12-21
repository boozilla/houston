package boozilla.houston.asset.sql;

public class From implements SqlStatement<Select> {
    private final Select select;
    private final String sheet;

    From(final Select select, final String sheet)
    {
        this.select = select;
        this.sheet = sheet;
    }

    public Where where(final String condition)
    {
        return Where.is(this, condition);
    }

    @Override
    public Select getParent()
    {
        return this.select;
    }

    @Override
    public String getSql()
    {
        return "FROM " + this.sheet;
    }

    @Override
    public String toString()
    {
        return getParent().toString() + " " + getSql();
    }
}
