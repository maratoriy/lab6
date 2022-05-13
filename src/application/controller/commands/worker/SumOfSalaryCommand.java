package application.controller.commands.worker;

import application.controller.view.ConsolePrinter;
import application.model.collection.CollectionManager;
import application.model.data.worker.Worker;
import application.controller.commands.AbstractCommand;
import application.controller.commands.CommandParameters;
import application.controller.commands.exceptions.CommandException;

import java.util.ArrayList;
import java.util.List;

public class SumOfSalaryCommand<T extends Worker> extends AbstractCommand {
    private final CollectionManager<T> collectionManager;

    public SumOfSalaryCommand(CollectionManager<T> collectionManager) {
        super("sum_of_salary", "display sum of salary");
        this.collectionManager = collectionManager;
    }

    @Override
    public void execute(CommandParameters params) throws CommandException {
        List<T> collectionItems = collectionManager.asList();
        Double sum = collectionItems.stream().collect(
                ArrayList<Double>::new,
                (list, item) -> list.add(item.getSalary()),
                ArrayList::addAll
        ).stream().reduce(Double::sum).orElse(0D);
        ConsolePrinter.print(String.format("Sum of salary equals %s\n", sum));
    }
}
