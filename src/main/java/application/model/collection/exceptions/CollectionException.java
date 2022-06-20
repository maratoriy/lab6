package application.model.collection.exceptions;

public class CollectionException extends RuntimeException {

    public CollectionException(String message) {
        super(message);
    }

    public CollectionException(Throwable cause) {
        super(String.format("Collection exception! Cause: %s", cause));
    }

    public CollectionException(String message, Throwable cause) {
        super(String.format("%s. Cause: %s", message, cause.getMessage()));
    }

}
