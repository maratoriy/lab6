package application.controller.commands;

import application.controller.commands.exceptions.NoSuchCommandException;
import application.controller.view.StringTable;

import java.util.*;

public class CommandManager {
    private final Map<String, Command> commandMap;

    public CommandManager() {
        commandMap = new LinkedHashMap<>();
    }

    public void addCommand(Command command) {
        commandMap.put(command.getName(), command);
    }

    public void execCommand(String commandName, CommandParameters parameters) {
        if (commandMap.containsKey(commandName))
            commandMap.get(commandName).execute(parameters);
        else throw new NoSuchCommandException();
    }

    public StringTable getInfoTable() {
        return new StringTable() {
            @Override
            public List<String> getTitles() {
                return Arrays.asList(
                        "name",
                        "description"
                );
            }

            @Override
            public List<Map<String, String>> getTable() {
                return commandMap.values().stream().collect(
                        ArrayList::new,
                        (list, item) -> list.add(StringTable.stringMatrixToMap(new String[][]{
                                {"name", item.getName()},
                                {"description", item.getDescription()}
                        })),
                        ArrayList::addAll
                );
            }
        };
    }
}
