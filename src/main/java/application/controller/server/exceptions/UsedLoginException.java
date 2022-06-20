package application.controller.server.exceptions;

public class UsedLoginException extends AuthorizationException {
    public UsedLoginException() {
        super("Already have user with this login!");
    }
}
