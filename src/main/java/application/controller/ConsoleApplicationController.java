package application.controller;

import application.controller.actions.Action;
import application.controller.actions.ActionManager;
import application.controller.commands.CommandManager;
import application.controller.commands.CommandParameters;
import application.controller.commands.basic.*;
import application.controller.commands.exceptions.CommandException;
import application.controller.exceptions.CriticalErrorException;
import application.controller.input.*;
import application.controller.input.exceptions.EndOfTheScriptException;
import application.controller.input.exceptions.OverLoadedScriptException;
import application.model.collection.CollectionItem;
import application.model.collection.CollectionManager;
import application.model.collection.exceptions.CollectionException;
import application.model.data.exceptions.InvalidDataException;
import application.view.console.ConsolePrinter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConsoleApplicationController<T extends CollectionItem> implements SavableController {

    private State state;

    private final ActionManager actionManager;
    private final CommandManager commandManager;
    private final InputManager inputManager;
    private final CollectionManager<T> collectionManager;
    private final Thread shutdownHook = new Thread(this::close);
    private String savePath;


    {
        actionManager = new ActionManager();
        commandManager = new CommandManager();
        inputManager = new InputManager(new ConsoleInputStrategy());
    }

    public ConsoleApplicationController(CollectionManager<T> collectionManager) {
        this.collectionManager = collectionManager;

        commandManager.addCommand(new HelpCommand(commandManager));
        commandManager.addCommand(new ShowCommand(collectionManager));
        commandManager.addCommand(new InfoCommand(collectionManager));
//        commandManager.addCommand(new SortCommand(collectionManager));
//        commandManager.addCommand(new ReorderCommand(collectionManager));
//        commandManager.addCommand(new ExitCommand(this));
        commandManager.addCommand(new ClearCommand(collectionManager));
        commandManager.addCommand(new RemoveByIdCommand(collectionManager));
        commandManager.addCommand(new AddCommand<>(collectionManager, actionManager, inputManager));
        commandManager.addCommand(new UpdateCommand<>(collectionManager, actionManager, inputManager));
//        commandManager.addCommand(new InsertAtCommand<>(collectionManager, actionManager, inputManager));
        commandManager.addCommand(new ExecuteScriptCommand(inputManager));
//        commandManager.addCommand(new SaveCommand(this, collectionManager));

        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    public ActionManager getActionManager() {
        return actionManager;
    }

    public void run() {
        class CommandAction implements Action {
            @Override
            public void act() {
//                ConsolePrinter.request("Enter command: ");
                String command = inputManager.getLine();
                if (command == null) return;
                List<String> commandLine = new ArrayList<>(Arrays.asList(command.split(" ")));
                String commandName = commandLine.size() != 0 ? commandLine.remove(0) : "";
                commandManager.execCommand(commandName, new CommandParameters(commandLine));
            }
        }

        state = State.RUNNING;
        while (state == State.RUNNING) {
            try {
                actionManager.act();
                actionManager.poll();
                if (!collectionManager.isInit()) actionManager.addFirst(collectionManager::init);
                if (actionManager.size() == 0) actionManager.add(new CommandAction());
            } catch (CommandException | CollectionException e) {
                if (inputManager.getType() == StrategyType.SCRIPT) consoleMode();
                ConsolePrinter.printError(e.getMessage());
                actionManager.clear();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (InvalidDataException e) {
                if (inputManager.getType() == StrategyType.SCRIPT) consoleMode();
                ConsolePrinter.printError(e.getMessage());
            } catch (EndOfTheScriptException | OverLoadedScriptException e) {
                consoleMode();
                ConsolePrinter.printError(e.getMessage());
            } catch (CriticalErrorException e) {
                ConsolePrinter.printError(e.getMessage());
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
                close();
            }
        }
        ConsolePrinter.print("Closing app...");
    }

    private InputStrategy defaultStrategy = new ConsoleInputStrategy();

    public void setDefaultStrategy(InputStrategy defaultStrategy) {
        this.defaultStrategy = defaultStrategy;
        inputManager.setStrategy(defaultStrategy);
    }

    public void consoleMode() {
        inputManager.setStrategy(defaultStrategy);
        ScriptInputStrategy.clearStack();
        actionManager.clear();
        ConsolePrinter.printBlock = false;
    }

    @Override
    public boolean isRunning() {
        return state == State.RUNNING;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public InputManager getInputManager() {
        return inputManager;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    @Override
    public String getSavePath() {
        return savePath;
    }

    public void close() {
        state = State.CLOSING;
        collectionManager.close();
    }
}
