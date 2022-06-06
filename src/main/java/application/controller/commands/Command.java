package application.controller.commands;

public interface Command {
    String getName();

    String getDescription();

    void execute(CommandParameters params);
}
