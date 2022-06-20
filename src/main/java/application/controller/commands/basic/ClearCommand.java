package application.controller.commands.basic;

import application.controller.commands.AbstractCommand;
import application.controller.commands.CommandParameters;
import application.model.collection.CollectionManager;
import application.view.console.ConsolePrinter;

public class ClearCommand extends AbstractCommand {
    private final CollectionManager<?> collectionManager;

    public ClearCommand(CollectionManager<?> collectionManager) {
        super("clear");
        this.collectionManager = collectionManager;
    }

    @Override
    public void execute(CommandParameters params) {
        collectionManager.clear();
        ConsolePrinter.print("Collection was cleared!\n");
    }
}
