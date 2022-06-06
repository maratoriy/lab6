package application.model.collection;

import application.model.collection.exceptions.InvalidIndexException;
import application.model.collection.exceptions.NoSuchElemException;

import java.time.LocalDateTime;
import java.util.*;

abstract public class StackCollectionManager<T extends CollectionItem> extends AbstractCollectionManager<T> {

    private final Stack<T> stack;
    {
        stack = new Stack<>();

    }

    public static void main(String[] args) {

    }

    @Override
    public void add(T element) {
        if(stack.stream().anyMatch(item -> item.getId().equals(element.getId()))) element.setId(generateId());
        stack.push(element);
    }

    @Override
    protected Collection<T> getCollection() {
        return Collections.synchronizedCollection(stack);
    }

    @Override
    public void reverse() {
        Collections.reverse(stack);
    }

    @Override
    public void sort() {
        Collections.sort(stack);
    }

    @Override
    protected StackCollectionInfoTable createInfoTable() {
        return new StackCollectionInfoTable(initializationTime, size(), size() != 0 ? "id = " + stack.peek().getId().toString() : "null");
    }

    @Override
    public void insertAtIndex(int index, T item) {
        if (index < 0 || index > size()) throw new InvalidIndexException();
        stack.insertElementAt(item, index);
    }

    @Override
    public void updateById(Long id, T item) {
        if (stack.stream().noneMatch(iter -> iter.getId().equals(id))) throw new NoSuchElemException();
        stack.replaceAll(iter -> (iter.getId().equals(id)) ? item : iter);
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
