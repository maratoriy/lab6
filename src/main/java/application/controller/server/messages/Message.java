package application.controller.server.messages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Message implements Serializable {
    private final Map<String, Object> data;
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
        READY,
        CLEAR,

        COMMAND,
        SUCCESS,

        ERROR,
        DATA,

        AUTH
    }


    static public String shorterString(String string, int num) {
        return (string.length() > num) ? string.substring(0, num) + "..." : string;
    }

    static public Message error(RuntimeException e) {
        return new Message(Type.ERROR).put("error", e).put("errorClass", e.getClass().getSimpleName());
    }

    protected String getDataString() {
        List<String> dataList = data.keySet().stream().collect(
                ArrayList::new,
                (list, key) -> list.add(String.format("%s=%s", key, shorterString(data.get(key).toString(), 50))),
                ArrayList::addAll
        );
        return String.join(";", dataList);
    }

    @Override
    public String toString() {
        return String.format("Message{type=%s;data={%s}}", type, getDataString());
    }


}
