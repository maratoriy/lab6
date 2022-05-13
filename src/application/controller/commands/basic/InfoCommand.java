package application.controller.commands.basic;

import application.model.collection.CollectionManager;
import application.controller.commands.AbstractCommand;
import application.controller.commands.CommandParameters;
import application.controller.view.ConsolePrinter;
import application.controller.view.ConsoleTable;

public class InfoCommand extends AbstractCommand {
    private final CollectionManager<?> collectionManager;

    public InfoCommand(CollectionManager<?> collectionManager) {
        super("info", "print info about collection");
        this.collectionManager = collectionManager;
    }

    @Override
    public void execute(CommandParameters params) {
        ConsolePrinter.print(new ConsoleTable(collectionManager.getCollectionInfo()));
    }
}
