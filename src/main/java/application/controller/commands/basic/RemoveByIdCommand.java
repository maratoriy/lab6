package application.controller.commands.basic;

import application.model.collection.CollectionManager;
import application.controller.commands.AbstractCommand;
import application.controller.commands.CommandParameters;
import application.controller.commands.exceptions.InvalidCommandParamException;
import application.view.ConsolePrinter;

public class RemoveByIdCommand extends AbstractCommand {
    private final CollectionManager<?> collectionManager;

    public RemoveByIdCommand(CollectionManager<?> collectionManager) {
        super("remove_by_id", "remove element from the collection by id");
        this.collectionManager = collectionManager;
    }

    @Override
    public void execute(CommandParameters params) {
        try {
            Long id = Long.valueOf(params.getAt(0));
            if (collectionManager.removeById(id))
                ConsolePrinter.print("Element was removed!\n");
            else {
                ConsolePrinter.print("No such element with typed id!\n");
            }
        } catch (NumberFormatException e) {
            throw new InvalidCommandParamException("id");
        }
    }
}
