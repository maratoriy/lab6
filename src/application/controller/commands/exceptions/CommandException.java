package application.controller.commands.exceptions;

public class CommandException extends RuntimeException {
    public CommandException(String message) {
        super(message);
    }

}
