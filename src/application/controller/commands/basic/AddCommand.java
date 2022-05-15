package application.controller.commands.basic;

import application.model.collection.CollectionItem;
import application.model.collection.CollectionManager;
import application.model.data.exceptions.UniqueFieldException;
import application.controller.actions.ActionManager;
import application.controller.commands.AbstractCommand;
import application.controller.commands.CommandParameters;
import application.controller.input.InputManager;
import application.view.ConsolePrinter;

public class AddCommand<T extends CollectionItem> extends AbstractCommand {
    private final CollectionManager<T> collectionManager;
    private final ActionManager actionManager;
    private final InputManager inputManager;

    public AddCommand(CollectionManager<T> collectionManager, ActionManager actionManager, InputManager inputManager) {
        super("add", "add new item to the collection");
        this.collectionManager = collectionManager;
        this.actionManager = actionManager;
        this.inputManager = inputManager;
    }

    @Override
    public void execute(CommandParameters params) {
        T newItem = collectionManager.generateNew();
        newItem.getNullGroupsNames().forEach(groupName -> {
            ConsolePrinter.request(String.format("Generate %s (yes/no): ", groupName));
            if (inputManager.getLine().equals("yes")) newItem.generateField(groupName);
        });
        newItem.getSettersNames().forEach(valueName -> actionManager.add(() -> {
            ConsolePrinter.request(String.format("Enter %s: ", valueName));
            String respond = inputManager.getLine();
            respond = respond.equals("") ? null : respond;
            if (newItem.getUniqueFields().contains(valueName)
                    && collectionManager.countByValue(valueName, respond) >= 1)
                throw new UniqueFieldException(valueName, respond);
            newItem.setValue(valueName, respond);
        }));
        actionManager.add(() -> collectionManager.add(newItem));
    }
}
