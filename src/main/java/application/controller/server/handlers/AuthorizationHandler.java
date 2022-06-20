package application.controller.server.handlers;

import application.controller.server.AuthorisationManager;
import application.controller.server.client.ServerClient;
import application.controller.server.exceptions.AuthorizationException;
import application.controller.server.exceptions.UsedLoginException;
import application.controller.server.exceptions.WrongLoginException;
import application.controller.server.exceptions.WrongPasswordException;
import application.controller.server.messages.ClientMessage;
import application.controller.server.messages.Message;
import application.model.collection.database.Database;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthorizationHandler extends PipeTypeAction {

    public AuthorizationHandler() {
        super(Message.Type.AUTH);
    }

    static public ResultSet selectUsers(Database database, String login) throws SQLException {
        return database.executeQuery("SELECT * FROM users WHERE \"user\" = ?", login);
    }

    static public String hashPassword(String password, String salt) {
        return Message.shorterString(AuthorisationManager.encryptMD5(password, salt), 511);
    }

    synchronized static public void login(String login, String password) {
        try {
            Database database = Database.getInstance();
            ResultSet resultSet = selectUsers(database, login);
            if (!resultSet.next()) throw new WrongLoginException();
            String dataBasePassword = resultSet.getString("password");
            String salt = resultSet.getString("salt");
            String hashedPassword = hashPassword(password, salt);
            if (!hashedPassword.equals(dataBasePassword)) throw new WrongPasswordException();
        } catch (SQLException e) {
            throw new AuthorizationException("SQL exception happened while authorization!");
        }
    }

    synchronized static public void register(String login, String password) {
        try {
            Database database = Database.getInstance();
            ResultSet resultSet = selectUsers(database, login);
            if (resultSet.next()) throw new UsedLoginException();
            String salt = String.valueOf((int) ((Math.random() * 999)));
            String hashedPassword = hashPassword(password, salt);
            int request = database.executeUpdate("INSERT INTO users VALUES (?, ?, ?)", login, hashedPassword, salt);
        } catch (SQLException e) {
            throw new AuthorizationException("SQL exception happened while authorization!");
        }
    }

    @Override
    protected void handleAction(ServerClient client, ClientMessage message) {
        try {
            String action = (String) message.get("action");
            String login = message.login;
            String password = message.password;
            if (action.equals("login")) login(login, password);
            if (action.equals("register")) register(login, password);
            client.setName(login);
            client.sendObject(new Message(Message.Type.SUCCESS).put("accepted", true));
        } catch (RuntimeException e) {
            client.sendObject(Message.error(e).put("accepted", false));
        }
    }
}
