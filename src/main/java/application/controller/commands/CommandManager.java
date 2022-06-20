package application.controller.commands;

import application.controller.commands.exceptions.NoSuchCommandException;
import application.view.datamodels.StringTable;

import java.util.*;

public class CommandManager {
    private final Map<String, Command> commandMap;
    private final Stack<String> history = new Stack<>();

    public CommandManager() {
        commandMap = new LinkedHashMap<>();
    }

    public void addCommand(Command command) {
        commandMap.put(command.getName(), command);
    }

    public void execCommand(String commandName, CommandParameters parameters) {
        if (commandName.equals("Skip")) return;
        if (commandMap.containsKey(commandName)) {
            history.addAll(bufHistory);
            bufHistory.clear();
            if (parameters.params.size() > 0) {
                history.push(commandName + " " + parameters.getLine());
            } else {
                history.push(commandName);
            }
            commandMap.get(commandName).execute(parameters);
        } else throw new NoSuchCommandException();
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

    private final Stack<String> bufHistory = new Stack<>();
    private final int pointer = 0;

    public String up(String last) {
        if (history.size() == 0)  return last;
        if (history.size() == 1) return history.peek();
        else {
            String item = history.pop();
            bufHistory.push(item);
            return item;
        }
    }

    public String down(String last) {
        if (history.size() == 0)  return last;
        if (bufHistory.size() == 0) return history.peek();
        else {
            String item = bufHistory.pop();
            history.push(item);
            return item;
        }
    }

}
