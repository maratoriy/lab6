package application.controller.commands.exceptions;

public class OpenFileException extends CommandException {
    public OpenFileException(String absPath, String cause) {
        super(String.format("Couldn't open the file %s. Cause: %s", absPath, cause));
    }
}
