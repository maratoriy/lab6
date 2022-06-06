package application.controller.exceptions;

public class CriticalErrorException extends RuntimeException {
    public CriticalErrorException(String message) {
        super(message);
    }

    public CriticalErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
