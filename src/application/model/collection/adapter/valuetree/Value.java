package application.model.collection.adapter.valuetree;

import application.model.data.exceptions.InvalidDataException;
import application.model.parse.DomParseable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.time.format.DateTimeParseException;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Value extends ValueNode {

    private boolean unique = false;
    private final StringSetter setter;
    private final StringGetter getter;

    @FunctionalInterface
    public interface StringSetter extends Consumer<String>, Serializable {
    }

    @FunctionalInterface
    public interface StringGetter extends Supplier<String>, Serializable {
    }

    public Value(String name, StringGetter getter, StringSetter setter) {
        super(name, NodeType.VALUE);
        this.setter = setter;
        this.getter = getter;
    }

    public void setValue(String value) {
        try {
            if (!parent.isGenerated()) parent.isNullGenerate();
            setter.accept(value);
        } catch (IllegalArgumentException | DateTimeParseException | InvalidDataException | NullPointerException e) {
            throw new InvalidDataException(e);
        }
    }

    public String getValue() {
        try {
            String get = getter.get();
            return get != null ? get : "null";
        } catch (NullPointerException e) {
            return "null";
        }
    }

    @Override
    public Value block() {
        super.block();
        return this;
    }

    public Value setUnique() {
        unique = true;
        return this;
    }

    public boolean isUnique() {
        return unique;
    }

    @Override
    public Element parse(Document document) {
        return DomParseable.createTextNode(document, getName(), getValue());
    }

    @Override
    public void parse(Element element) {

    }
}
