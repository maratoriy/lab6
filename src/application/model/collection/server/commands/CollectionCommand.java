package application.model.collection.server.commands;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class CollectionCommand implements Serializable {
    private final String name;
    private final Map<String, Object> data;

    {
        data = new HashMap<>();
    }

    public CollectionCommand(String name) {
        this.name = name;
    }

    public CollectionCommand put(String valueName, Object value) {
        data.put(valueName, value);
        return this;
    }

    public Object get(String valueName) {
        return data.get(valueName);
    }

    public String getName() {
        return name;
    }
}
