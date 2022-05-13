package application.model.collection.adapter.valuetree;

import java.io.Serializable;

@FunctionalInterface
public interface ValueGenerator extends Serializable {
    void generate();
}