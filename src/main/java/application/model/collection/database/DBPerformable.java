package application.model.collection.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface DBPerformable {
    static DBRequest selectAllDB(String db_name, String user) {
        String sql = "SELECT * FROM " + db_name + " WHERE \"user\" = ?";
        return new DBRequest(sql, user);
    }

    static DBRequest deleteAllDB(String db_name, String user) {
        String sql = "DELETE FROM " + db_name + " WHERE \"user\" = ?";
        return new DBRequest(sql, user);
    }

    static DBRequest deleteAllCompletelyDB(String db_name) {
        return new DBRequest("DELETE FROM " + db_name);
    }


    List<DBRequest> deleteAll(String db_name, String user);

    List<DBRequest> deleteAllCompletely(String db_Name);

    List<DBRequest> insert(String db_name);

    List<DBRequest> update(String db_name, boolean userMode);

    List<DBRequest> delete(String db_name, boolean userMode);

    void parse(ResultSet resultSet) throws SQLException;
}
