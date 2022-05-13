package application.controller.server.exceptions;

public class ServerException extends RuntimeException {
    public ServerException(String message) {
        super(message);
    }

    public ServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServerException(Throwable cause) {
        super(String.format("Server exception! Cause %s", cause.getMessage()), cause);
    }
}
