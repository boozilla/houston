package boozilla.houston.asset.sql;

public interface SqlStatement<T> {
    T getParent();

    String getSql();

    default Offset offset()
    {
        return new Offset(null, 0, 0);
    }
}
