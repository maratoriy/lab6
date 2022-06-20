package application.controller.server.messages;

public class ClientMessage extends Message {
    public String login;
    public String password;

    public ClientMessage(Type type) {
        super(type);
    }

    public ClientMessage(Type type, String login, String password) {
        super(type);
        this.login = login;
        this.password = password;
    }

    @Override
    public ClientMessage put(String key, Object value) {
        return (ClientMessage) super.put(key, value);
    }

    @Override
    public String toString() {
        return String.format("ClientMessage{login=%s;password=%s;type=%s;data={%s}}", login, password, getType(), getDataString());
    }
}
