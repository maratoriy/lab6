package application.model.collection;

import application.model.collection.exceptions.InvalidIndexException;
import application.model.collection.exceptions.NoSuchElemException;

import java.time.LocalDateTime;
import java.util.*;

abstract public class StackCollectionManager<T extends CollectionItem> extends AbstractCollectionManager<T> {

    private final Stack<T> collection;

    {
        collection = new Stack<>();
    }

    public static void main(String[] args) {

    }

    @Override
    public void add(T element) {
        if(collection.stream().anyMatch(item -> item.getId().equals(element.getId()))) element.setId(generateId());
        collection.push(element);
    }

    @Override
    protected Collection<T> getCollection() {
        return collection;
    }

    @Override
    public void reverse() {
        Collections.reverse(collection);
    }

    @Override
    public void sort() {
        Collections.sort(collection);
    }

    @Override
    protected StackCollectionInfoTable createInfoTable() {
        return new StackCollectionInfoTable(initializationTime, size(), size() != 0 ? "id = " + collection.peek().getId().toString() : "null");
    }

    @Override
    public void insertAtIndex(int index, T item) {
        if (index < 0 || index > size()) throw new InvalidIndexException();
        collection.insertElementAt(item, index);
    }

    @Override
    public void updateById(Long id, T item) {
        if (collection.stream().noneMatch(iter -> iter.getId().equals(id))) throw new NoSuchElemException();
        collection.replaceAll(iter -> (iter.getId().equals(id)) ? item : iter);
    }

    static protected class StackCollectionInfoTable extends CollectionInfoTable {
        private final String idTop;

        public StackCollectionInfoTable(LocalDateTime initializationTime, int size, String topId) {
            super(initializationTime, size);
            this.idTop = topId;
        }

        @Override
        public List<String> getTitles() {
            List<String> titles = super.getTitles();
            titles.add("top of the stack");
            return titles;
        }

        @Override
        public List<Map<String, String>> getTable() {
            List<Map<String, String>> table = super.getTable();
            table.get(0).put("top of the stack", idTop);
            return table;
        }
    }

}
