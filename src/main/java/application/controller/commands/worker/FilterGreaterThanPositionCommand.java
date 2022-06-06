package application.controller.commands.worker;

import application.controller.commands.exceptions.InvalidCommandParamException;
import application.view.ConsolePrinter;
import application.view.ConsoleTable;
import application.view.StringTable;
import application.model.collection.AbstractCollectionManager;
import application.model.collection.CollectionManager;
import application.model.data.worker.Position;
import application.model.data.worker.Worker;
import application.controller.commands.AbstractCommand;
import application.controller.commands.CommandParameters;
import application.controller.commands.exceptions.CommandException;


public class FilterGreaterThanPositionCommand<T extends Worker> extends AbstractCommand {
    private final CollectionManager<T> collectionManager;

    public FilterGreaterThanPositionCommand(CollectionManager<T> collectionManager) {
        super("filter_greater_than_position", "display elements with position greater than typed");
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