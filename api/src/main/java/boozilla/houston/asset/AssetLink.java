package boozilla.houston.asset;

import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@Getter
@ToString
public class AssetLink {
    private final String sheetName;
    private final String columnName;
    private AssetLink related;

    public AssetLink(final houston.vo.asset.AssetLink link)
    {
        this.sheetName = link.getSheetName();
        this.columnName = link.getColumnName();

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
        this.columnName = columnName;
        this.related = link;
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
