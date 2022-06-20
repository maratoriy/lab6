package application.controller.commands.worker;

import application.controller.commands.AbstractCommand;
import application.controller.commands.CommandParameters;
import application.controller.commands.exceptions.CommandException;
import application.controller.commands.exceptions.InvalidCommandParamException;
import application.model.collection.AbstractCollectionManager;
import application.model.collection.CollectionManager;
import application.model.data.worker.Position;
import application.model.data.worker.Worker;
import application.view.console.ConsolePrinter;
import application.view.console.ConsoleTable;
import application.view.datamodels.StringTable;


public class FilterGreaterThanPositionCommand<T extends Worker> extends AbstractCommand {
    private final CollectionManager<T> collectionManager;

    public FilterGreaterThanPositionCommand(CollectionManager<T> collectionManager) {
        super("filter_greater_than_position");
        this.collectionManager = collectionManager;
    }

    @Override
    public void execute(CommandParameters params) throws CommandException {
        try {
            String typed = params.getAt(0);
            Position position = Position.valueOf(typed);
            StringTable filteredTable = AbstractCollectionManager.getCollectionTable(
                    collectionManager.asFilteredList(item ->
                            item.getPosition().ordinal() < position.ordinal())
            );
            ConsolePrinter.print(new ConsoleTable(filteredTable));
        } catch (IllegalArgumentException e) {
            throw new InvalidCommandParamException("position");
        }
    }
}
