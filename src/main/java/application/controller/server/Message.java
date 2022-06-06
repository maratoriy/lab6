package application.controller.server;

import application.controller.server.client.ServerClient;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Message implements Serializable {
    private Map<String, Object> data;
    private final Type type;

    public Message(Type type) {
        this.type = type;
    }

    {
        data = new HashMap<>();
    }

    public Message put(String key, Object value) {
        data.put(key, value);
        return this;
    }

    public Type getType() {
        return type;
    }

    public Object get(String key) {
        return data.get(key);
    }

    public enum Type {
        PARTS,
        COMMAND,
        SUCCESS,
        ERROR,
        DATA
    }

    static private String shorterString(String string, int num) {
        return (string.length()>num) ? string.substring(0, num)+"..." : string;
    }

    @Override
    public String toString() {
        List<String> dataList = data.keySet().stream().collect(
                ArrayList::new,
                (list, key) -> list.add(String.format("%s=%s", key, shorterString(data.get(key).toString(), 50))),
                ArrayList::addAll
        );
        String dataString = String.join(";", dataList);
        return String.format("Message{type=%s;data={%s}}", type, dataString);
    }

}
