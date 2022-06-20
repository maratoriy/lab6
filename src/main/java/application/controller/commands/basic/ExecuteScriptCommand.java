package application.controller.commands.basic;

import application.controller.commands.AbstractCommand;
import application.controller.commands.CommandParameters;
import application.controller.input.InputManager;
import application.controller.input.ScriptInputStrategy;
import application.view.console.ConsolePrinter;

public class ExecuteScriptCommand extends AbstractCommand {
    private final InputManager inputManager;

    public ExecuteScriptCommand(InputManager inputManager) {
        super("execute_script");
        this.inputManager = inputManager;
    }

    @Override
    public void execute(CommandParameters params) {
        inputManager.setStrategy(new ScriptInputStrategy(params.getLine()));
        ConsolePrinter.printBlock = true;
    }
}
