package application.controller.commands.exceptions;

public class NoSuchCommandException extends CommandException {
    public NoSuchCommandException() {
        super("No such command!");
    }

}
