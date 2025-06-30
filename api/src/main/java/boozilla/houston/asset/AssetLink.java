package boozilla.houston.asset;

import lombok.Getter;
import lombok.ToString;

import java.util.Objects;
import java.util.regex.Pattern;

@Getter
@ToString
public class AssetLink {
    private static final Pattern EXPRESSION_PATTERN = Pattern.compile("(.+)(\\[.+])");

    private final String sheetName;
    private final String columnName;
    private final String expression;
    private AssetLink related;

    public AssetLink(final houston.vo.asset.AssetLink link)
    {
        this.sheetName = link.getSheetName();

        final var parsed = parseColumnName(link.getColumnName());
        this.columnName = parsed[0];
        this.expression = parsed[1];

        if(link.hasRelated())
            this.related = new AssetLink(link.getRelated());
    }

    public AssetLink(final String sheetName, final String columnName)
    {
        this(sheetName, columnName, null);
    }

    public AssetLink(final String sheetName, final String columnName, final AssetLink link)
    {
        this.sheetName = sheetName;

        final var parsed = parseColumnName(columnName);
        this.columnName = parsed[0];
        this.expression = parsed[1];

        this.related = link;
    }

    private String[] parseColumnName(final String input)
    {
        if(Objects.isNull(input))
        {
            return new String[] {null, null};
        }

        final var matcher = EXPRESSION_PATTERN.matcher(input);

        if(matcher.matches())
        {
            return new String[] {matcher.group(1), matcher.group(2)};
        }

        return new String[] {input, null};
    }

    public AssetLink related(final String sheetName)
    {
        if(Objects.nonNull(this.getRelated()) && this.getRelated().getSheetName().contentEquals(sheetName))
        {
            return this;
        }

        return null;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(sheetName, columnName);
    }
}
