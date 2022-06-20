package application.controller.commands.basic;

import application.controller.commands.AbstractCommand;
import application.controller.commands.CommandParameters;
import application.model.collection.CollectionManager;
import application.view.console.ConsolePrinter;
import application.view.console.ConsoleTable;

public class InfoCommand extends AbstractCommand {
    private final CollectionManager<?> collectionManager;

    public InfoCommand(CollectionManager<?> collectionManager) {
        super("info");
        this.collectionManager = collectionManager;
    }

    @Override
    public void execute(CommandParameters params) {
        ConsolePrinter.print(new ConsoleTable(collectionManager.getCollectionInfo()));
    }
}
