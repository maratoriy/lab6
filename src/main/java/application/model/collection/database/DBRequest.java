package application.model.collection.database;

public class DBRequest {
    private final String sql;
    private final Object[] args;

    public DBRequest(String sql, Object... args) {
        this.sql = sql;
        this.args = args;
    }

    public String getSql() {
        return sql;
    }

    public Object[] getArgs() {
        return args;
    }
}
