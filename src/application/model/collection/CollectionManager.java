package application.model.collection;

import application.view.StringTable;

import java.io.Serializable;
import java.util.List;
import java.util.function.Predicate;

public interface CollectionManager<T extends CollectionItem> {
    default void init() {
    }

    default boolean isInit() {
        return true;
    }

    default void close() {
    }


    int size();


    void clear();

    void reverse();

    void sort();

    void insertAtIndex(int index, T item);

    void updateById(Long id, T item);

    long countByValue(String valueName, String value);

    void add(T element);

    Class<T> getElemsClass();

    T generateNew();

    T getById(Long id);

    boolean removeById(Long id);

    StringTable getCollectionTable();
    StringTable getCollectionInfo();

    List<T> asFilteredList(CollectionFilter<? super T> predicate);
    List<T> asList();


    interface CollectionFilter<T extends CollectionItem> extends Predicate<T>, Serializable {

    }
}
