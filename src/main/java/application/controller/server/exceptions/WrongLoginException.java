package application.controller.server.exceptions;

public class WrongLoginException extends AuthorizationException {
    public WrongLoginException() {
        super("Wrong login!");
    }
}
