package application.model.data.exceptions;

public class UniqueFieldException extends InvalidDataException {
    public UniqueFieldException(String valueName, String value) {
        super(String.format("Collection already have element with unique field \"%s\"=\"%s\"", valueName, value));
    }
}
