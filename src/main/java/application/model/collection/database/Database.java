package application.model.collection.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class Database {
    private static volatile Database instance;
    private final static Logger logger = LoggerFactory.getLogger(Database.class);
    private static final String CONFIG_FILE = "db.cfg";
    private final Connection connection;
    private PreparedStatement stmt;

    static String USER;
    static String PASS;


    private Database() throws SQLException, IOException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        FileInputStream fis;
        Properties property = new Properties();
        try {
            fis = new FileInputStream(CONFIG_FILE);
            property.load(fis);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Database.PASS = (property.getProperty("PASS"));
        Database.USER = property.getProperty("USER");
        String port = property.getProperty("PORT");
        String dbname = property.getProperty("DBNAME");

        String url = String.format("jdbc:postgresql://%s:5432/%s", port, dbname);
        logger.info(url);
        connection = DriverManager.getConnection(url, USER, PASS);
        logger.info(String.valueOf(connection));
    }


    public static Database getInstance() {
        if (instance == null) {
            synchronized (Database.class) {
                if (instance == null) {
                    try {
                        instance = new Database();
                    } catch (SQLException | IOException e) {
                        logger.error("database connection error:\n{}", e.getMessage());
                        System.exit(0);
                    }
                }
            }
        }
        return instance;
    }

    private PreparedStatement parseSql(String sql, Object[] args) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(sql);
        for (int i = 1; i <= args.length; i++) {
            Object arg = args[i - 1];
            if (arg == null) {
                stmt.setNull(i, Types.OTHER);
            } else if (arg instanceof String) {
                stmt.setString(i, arg.toString());
            } else if (arg instanceof Enum) {
                stmt.setObject(i, ((Enum<?>) arg).name(), Types.OTHER);
            } else if (arg instanceof Integer) {
                stmt.setInt(i, (Integer) arg);
            } else if (arg instanceof Long) {
                stmt.setLong(i, (Long) arg);
            } else if (arg instanceof Double) {
                stmt.setDouble(i, (Double) arg);
            } else if (arg instanceof Float) {
                stmt.setFloat(i, (Float) arg);
            } else if (arg instanceof LocalDate) {
                stmt.setObject(i, arg);
            } else if (arg instanceof Date) {
                stmt.setDate(i, new java.sql.Date(((Date) arg).getTime()));
            } else if (arg instanceof ZonedDateTime) {
                stmt.setDate(i, new java.sql.Date(Date.from(((ZonedDateTime) arg).toInstant()).getTime()));
            } else {
                stmt.close();
                throw new SQLException("unknown data type");
            }
        }
        return stmt;
    }

    public void executeUpdates(List<DBRequest> dbRequests) throws SQLException {
        for (DBRequest iter :
                dbRequests) {
            executeUpdate(iter);
        }
    }

    public List<ResultSet> executeQueries(List<DBRequest> dbRequests) throws SQLException {
        List<ResultSet> resultSets = new ArrayList<>();
        for (DBRequest iter :
                dbRequests) {
            resultSets.add(executeQuery(iter));
        }
        return resultSets;
    }

    public ResultSet executeQuery(DBRequest dbRequest) throws SQLException {
        return executeQuery(dbRequest.getSql(), dbRequest.getArgs());
    }

    public int executeUpdate(DBRequest dbRequest) throws SQLException {
        return executeUpdate(dbRequest.getSql(), dbRequest.getArgs());
    }

    public ResultSet executeQuery(String sql, Object... args) throws SQLException {
        stmt = parseSql(sql, args);
        return stmt.executeQuery();
    }

    public int executeUpdate(String sql, Object... args) throws SQLException {
        stmt = parseSql(sql, args);
        int answer = stmt.executeUpdate();
        stmt.close();
        return answer;
    }

    public void closeQuery() throws SQLException {
        stmt.close();
    }

    public void closeConnection() throws SQLException {
        connection.close();
    }
}