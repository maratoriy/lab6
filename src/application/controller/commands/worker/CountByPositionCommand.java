package application.controller.commands.worker;

import application.model.collection.CollectionManager;
import application.model.data.worker.Position;
import application.model.data.worker.Worker;
import application.controller.commands.AbstractCommand;
import application.controller.commands.CommandParameters;
import application.controller.commands.exceptions.CommandException;
import application.controller.commands.exceptions.InvalidCommandParamException;
import application.view.ConsolePrinter;


public class CountByPositionCommand<T extends Worker> extends AbstractCommand {
    private final CollectionManager<T> collectionManager;

    public CountByPositionCommand(CollectionManager<T> collectionManager) {
        super("count_by_position", "count element by typed position");
        this.collectionManager = collectionManager;
    }

    @Override
    public void execute(CommandParameters params) throws CommandException {
        try {
            Position position = Position.valueOf(params.getAt(0));
            long res = collectionManager.countByValue("Worker.position", position.toString());
            ConsolePrinter.print("Number of elements with typed position equals " + res + "\n");
        } catch (IllegalArgumentException e) {
            throw new InvalidCommandParamException("position");
        }
    }
}
