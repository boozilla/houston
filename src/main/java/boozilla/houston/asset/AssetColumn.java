package boozilla.houston.asset;

import lombok.Builder;

import java.util.Set;

@Builder
public class AssetColumn {
    private static final DataType PRIMARY_TYPE = DataType.LONG;

    private final int index;
    private final String comment;
    private final String name;
    private final DataType type;
    private final boolean array;
    private final Set<Scope> scope;
    private final boolean nullable;
    private final String link;

    public boolean isPrimary()
    {
        return name.toLowerCase().contentEquals("code");
    }

    public int index()
    {
        return index;
    }

    public String comment()
    {
        return comment;
    }

    public String name()
    {
        return name;
    }

    public DataType type()
    {
        return isPrimary() ? PRIMARY_TYPE : type;
    }

    public boolean array()
    {
        return array;
    }

    public Set<Scope> scope()
    {
        return scope;
    }

    public boolean isNullable()
    {
        return !isPrimary() && nullable;
    }

    public String link()
    {
        return link;
    }

    @Override
    public String toString()
    {
        return "{index=" + index + ", name=" + name + ", type=" + type() + ", array=" + array + ", scope=" + scope + ", nullable=" + isNullable() + ", link=" + link + "}";
    }

    public static class AssetColumnBuilder {
        public AssetColumnBuilder type(final String type)
        {
            this.array = type.toLowerCase().endsWith("[]");

            final var typeString = array ? type.substring(0, type.length() - 2) : type;

            return type(DataType.from(typeString));
        }

        public AssetColumnBuilder type(final DataType type)
        {
            this.type = type;
            return this;
        }

        public AssetColumnBuilder scope(final String scope)
        {
            this.scope = switch(scope.toLowerCase())
            {
                case "server" -> Set.of(Scope.SERVER);
                case "client" -> Set.of(Scope.CLIENT);
                default -> Set.of(Scope.SERVER, Scope.CLIENT);
            };

            return this;
        }
    }
}

