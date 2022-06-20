package application.controller.commands.basic;

import application.controller.commands.AbstractCommand;
import application.controller.commands.CommandManager;
import application.controller.commands.CommandParameters;
import application.view.console.ConsolePrinter;
import application.view.console.ConsoleTable;

public class HelpCommand extends AbstractCommand {
    private final CommandManager commandManager;

    public HelpCommand(CommandManager commandManager) {
        super("help");
        this.commandManager = commandManager;
    }

    @Override
    public void execute(CommandParameters params) {
        ConsolePrinter.print(new ConsoleTable(commandManager.getInfoTable()));
    }
}
