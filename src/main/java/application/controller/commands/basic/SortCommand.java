package application.controller.commands.basic;

import application.controller.commands.AbstractCommand;
import application.controller.commands.CommandParameters;
import application.model.collection.CollectionManager;
import application.view.console.ConsolePrinter;

public class SortCommand extends AbstractCommand {
    private final CollectionManager<?> collectionManager;

    public SortCommand(CollectionManager<?> collectionManager) {
        super("sort");
        this.collectionManager = collectionManager;
    }

    @Override
    public void execute(CommandParameters params) {
        collectionManager.sort();
        ConsolePrinter.print("Collection was sorted!\n");
    }
}
