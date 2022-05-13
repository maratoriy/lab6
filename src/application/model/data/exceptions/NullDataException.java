package application.model.data.exceptions;

public class NullDataException extends InvalidDataException {
    public NullDataException() {
        super("Field cannot be null");
    }
}
