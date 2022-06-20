package application.model.data.exceptions;

public class EmptyFieldException extends InvalidDataException {
    public EmptyFieldException() {
        super("Field cannot be empty");
    }
}
