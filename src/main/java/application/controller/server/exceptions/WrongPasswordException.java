package application.controller.server.exceptions;

public class WrongPasswordException extends AuthorizationException {
    public WrongPasswordException() {
        super("Wrong password!!");
    }
}
