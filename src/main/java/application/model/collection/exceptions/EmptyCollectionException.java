package application.model.collection.exceptions;

public class EmptyCollectionException extends CollectionException {
    public EmptyCollectionException() {
        super("Collection is empty!");
    }
}
