package boozilla.houston.asset.sql;

import com.google.protobuf.GeneratedMessageV3;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Select implements RootSqlStatement, SelectAll {
    private final Select instance;

    private final Set<String> columns;

    private Select()
    {
        this.columns = new HashSet<>();
        this.instance = this;
    }

    public static SelectAll all()
    {
        return columns("*");
    }

    public static Select columns(final String... name)
    {
        final var instance = new Select();
        instance.columns.addAll(Arrays.asList(name));

        return instance;
    }

    public From from(final Class<? extends GeneratedMessageV3> sheet)
    {
        return from(sheet.getSimpleName());
    }

    public From from(final String sheet)
    {
        return new From(this, sheet);
    }

    @Override
    public Void getParent()
    {
        return null;
    }

    @Override
    public String getSql()
    {
        return this.toString();
    }

    @Override
    public String toString()
    {
        return "SELECT " + String.join(", ", this.instance.columns);
    }
}
