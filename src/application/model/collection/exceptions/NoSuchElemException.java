package application.model.collection.exceptions;

public class NoSuchElemException extends CollectionException {
    public NoSuchElemException() {
        super("No such element in collection!");
    }
}
