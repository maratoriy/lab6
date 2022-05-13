package application.controller.commands.exceptions;

public class InvalidCommandParamException extends CommandException {
    public InvalidCommandParamException(String param) {
        super(String.format("Invalid param \"%s\" of the typed command!", param));
    }
}
