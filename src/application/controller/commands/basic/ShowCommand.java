package application.controller.commands.basic;

import application.model.collection.CollectionManager;
import application.controller.commands.AbstractCommand;
import application.controller.commands.CommandParameters;
import application.controller.view.ConsolePrinter;
import application.controller.view.ConsoleTable;

public class ShowCommand extends AbstractCommand {
    private final CollectionManager<?> collectionManager;

    public ShowCommand(CollectionManager<?> collectionManager) {
        super("show", "show all elements");
        this.collectionManager = collectionManager;
    }

    @Override
    public void execute(CommandParameters params) {
        ConsolePrinter.print(new ConsoleTable(collectionManager.getCollectionTable()).setTableName("Elements of the collection"));
    }
}
