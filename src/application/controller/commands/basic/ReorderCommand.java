package application.controller.commands.basic;

import application.model.collection.CollectionManager;
import application.controller.commands.AbstractCommand;
import application.controller.commands.CommandParameters;
import application.controller.view.ConsolePrinter;

public class ReorderCommand extends AbstractCommand {
    private final CollectionManager<?> collectionManager;

    public ReorderCommand(CollectionManager<?> collectionManager) {
        super("reorder", "reorder the collection");
        this.collectionManager = collectionManager;
    }

    @Override
    public void execute(CommandParameters params) {
        collectionManager.reverse();
        ConsolePrinter.print("Collection was reordered!\n");
    }
}
