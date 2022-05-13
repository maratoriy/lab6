package application.model.data.exceptions;

public class InvalidDataException extends RuntimeException {
    public InvalidDataException(String message) {
        super(message);
    }

    public InvalidDataException(Throwable cause) {
        super(String.format("Invalid data format! Cause: %s", cause.getMessage()), cause);
    }
}
