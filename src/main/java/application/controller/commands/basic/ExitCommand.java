package application.controller.commands.basic;

import application.controller.BasicController;
import application.controller.commands.AbstractCommand;
import application.controller.commands.CommandParameters;

public class ExitCommand extends AbstractCommand {
    private final BasicController controller;

    public ExitCommand(BasicController controller) {
        super("exit", "exit without saving");
        this.controller = controller;
    }

    @Override
    public void execute(CommandParameters params) {
        controller.close();
    }
}
