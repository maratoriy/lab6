package application.controller.commands;

import application.controller.commands.exceptions.NoSuchParamException;

import java.util.List;

public class CommandParameters {
    public final List<String> params;

    public CommandParameters(List<String> params) {
        this.params = params;
    }

    public String getLine() {
        return String.join(" ", params);
    }

    public String getAt(int index) {
        if (params.size() <= index) throw new NoSuchParamException();
        return params.get(index);
    }
}
