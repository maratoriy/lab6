package application.model.data.exceptions;

public class NoSuchFieldException extends InvalidDataException {
    public NoSuchFieldException() {
        super("No such field in collection item");
    }
}
