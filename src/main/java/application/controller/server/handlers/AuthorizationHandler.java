package application.controller.server.handlers;

import application.controller.server.Message;
import application.controller.server.client.ServerClient;
import application.model.collection.server.AuthorisationManager;
import application.model.collection.database.Database;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthorizationHandler extends AbstractMessageHandler {

    public AuthorizationHandler() {
        super(Message.Type.AUTH);
    }

    static public ResultSet selectUsers(Database database, String login) throws SQLException {
        return database.executeQuery("SELECT * FROM users WHERE \"user\" = ?", login);
    }

    static public String hashPassword(String password, String salt) {
        return Message.shorterString(AuthorisationManager.encryptMD5(password, salt), 511);
    }

    static public boolean login(String login, String password)  {
        try {
            Database database = Database.getInstance();
            ResultSet resultSet = selectUsers(database, login);
            resultSet.next();
            String dataBasePassword = resultSet.getString("password");
            String salt = resultSet.getString("salt");
            String hashedPassword = hashPassword(password, salt);
            return (hashedPassword.equals(dataBasePassword));
        } catch (SQLException e) {
            return false;
        }
    }

    static public boolean register(String login, String password) {
        try {
            Database database = Database.getInstance();
            ResultSet resultSet = selectUsers(database, login);
            if(resultSet.next()) return false;
            String salt =  String.valueOf((int) ((Math.random() * 999)));
            String hashedPassword = hashPassword(password, salt);
            int request = database.executeUpdate("INSERT INTO users VALUES (?, ?, ?)", login, hashedPassword, salt);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    protected void handleAction(ServerClient client, Message message) {
        try {
            String action = (String) message.get("action");
            String login = (String) message.get("login");
            String password = (String) message.get("password");
            boolean result = false;
            if (action.equals("login")) result = login(login, password);
            if (action.equals("register")) result = register(login, password);
            client.sendObject(new Message(Message.Type.SUCCESS).put("accepted", result));
        } catch (RuntimeException e) {
            client.sendObject(new Message(Message.Type.SUCCESS).put("accepted", false));
        }
    }
}
