package application.controller.commands.exceptions;

public class NoSuchParamException extends CommandException {
    public NoSuchParamException() {
        super("No required param in typed command!");
    }
}
