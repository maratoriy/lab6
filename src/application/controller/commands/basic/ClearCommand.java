package application.controller.commands.basic;

import application.model.collection.CollectionManager;
import application.controller.commands.AbstractCommand;
import application.controller.commands.CommandParameters;
import application.view.ConsolePrinter;

public class ClearCommand extends AbstractCommand {
    private final CollectionManager<?> collectionManager;

    public ClearCommand(CollectionManager<?> collectionManager) {
        super("clear", "deletes all the elements from collection");
        this.collectionManager = collectionManager;
    }

    @Override
    public void execute(CommandParameters params) {
        collectionManager.clear();
        ConsolePrinter.print("Collection was cleared!\n");
    }
}
