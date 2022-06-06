package application.controller.server.exceptions;

import application.controller.commands.exceptions.CommandException;

public class ServerException extends CommandException {
    public ServerException(String message) {
        super(message);
    }

    public ServerException(Throwable cause) {
        super(String.format("Server exception! Cause %s", cause.getMessage()));
    }
}
