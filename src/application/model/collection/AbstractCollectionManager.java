package application.model.collection;

import application.model.collection.exceptions.EmptyCollectionException;
import application.model.collection.exceptions.NoSuchElemException;
import application.model.parse.DomParseable;
import application.controller.view.StringTable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract public class AbstractCollectionManager<T extends CollectionItem> implements CollectionManager<T>, DomParseable {
    protected LocalDateTime initializationTime;

    {
        initializationTime = LocalDateTime.now();
    }

    abstract protected Collection<T> getCollection();

    @Override
    public long countByValue(String valueName, String value) {
        return getFiltered(iter -> iter.getGettersNames().contains(valueName) && iter.getValue(valueName).equals(value)).count();
    }


    protected Long generateId() {
        List<Long> idList = getCollection().stream().collect(
                ArrayList::new,
                (list, item) -> list.add(item.getId()),
                ArrayList::addAll
        );
        Collections.sort(idList);
        if (idList.size() == 0) return 1L;
        for (int i = 1; i < idList.size(); i++) {
            if (idList.get(i) - idList.get(i - 1) > 1L) return idList.get(i - 1) + 1L;
        }
        return idList.get(idList.size() - 1) + 1L;
    }

    private Stream<T> getFiltered(Predicate<? super T> filter) {
        return getCollection().stream().filter(filter);
    }

    @Override
    public Element parse(Document document) {
        Element root = document.createElement("collection");
        root.appendChild(DomParseable.createTextNode(document, "initializationTime", initializationTime.toString()));
        root.appendChild(DomParseable.createTextNode(document, "count", String.valueOf(size())));

        Element elements = document.createElement("elements");
        for (T iter : getCollection()) {
            Element element = iter.parse(document);
            elements.appendChild(element);
        }
        root.appendChild(elements);
        return root;
    }

    @Override
    public void parse(Element element) {
        NodeList nodeList = element.getElementsByTagName("elements").item(0).getChildNodes();
        initializationTime = LocalDateTime.parse(element.getElementsByTagName("initializationTime").item(0).getTextContent());
        for (int i = 0; i < nodeList.getLength(); i++) {
            T t = generateNew();
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) node;
                t.parse(eElement);
                add(t);
            }
        }
    }

    @Override
    public T getById(Long id) {
        Optional<T> equalIdItem = getFiltered(iter -> iter.getId().equals(id)).findFirst();
        if (equalIdItem.isPresent())
            return equalIdItem.get();
        else throw new NoSuchElemException();
    }

    @Override
    public boolean removeById(Long id) {
        return getCollection().removeIf(iter -> iter.getId().equals(id));
    }

    @Override
    public int size() {
        return getCollection().size();
    }

    @Override
    public void clear() {
        getCollection().clear();
    }

    @Override
    public List<T> asFilteredList(CollectionFilter<? super T> predicate) {
        return getCollection().stream().filter(predicate).collect(Collectors.toList());
    }

    @Override
    public List<T> asList() {
        return new ArrayList<>(getCollection());
    }

    @Override
    public StringTable getCollectionTable() {
        return getCollectionTable(getCollection());
    }

    public static <T extends CollectionItem> StringTable getCollectionTable(Collection<T> collection) {
        if (collection.size() == 0) throw new EmptyCollectionException();
        return new StringTable() {
            @Override
            public List<String> getTitles() {
                return collection.stream().collect(
                        LinkedHashSet<String>::new,
                        (list, item) -> {
                            list.addAll(item.getGettersNames());
                        },
                        LinkedHashSet::addAll
                ).stream().map(str -> str.substring(str.indexOf('.') + 1)).collect(Collectors.toList());
            }

            @Override
            public List<Map<String, String>> getTable() {
                return collection.stream().collect(
                        ArrayList::new,
                        (list, item) ->
                                list.add(item.getGettersNames().stream().collect(
                                        Collectors.toMap(
                                                valueName -> valueName.substring(valueName.indexOf('.') + 1),
                                                item::getValue))),
                        ArrayList::addAll
                );
            }
        };
    }


    @Override
    public StringTable getCollectionInfo() {
        return createInfoTable();
    }

    protected CollectionInfoTable createInfoTable() {
        return new CollectionInfoTable(initializationTime, size());
    }


    static protected class CollectionInfoTable implements StringTable {
        private final String initializationTime;
        private final String size;

        public CollectionInfoTable(LocalDateTime initializationTime, int size) {
            this.initializationTime = initializationTime.toString();
            this.size = String.valueOf(size);
        }

        @Override
        public List<String> getTitles() {
            return new ArrayList<>(Arrays.asList(
                    "initializationTime",
                    "size"
            ));
        }

        @Override
        public List<Map<String, String>> getTable() {
            List<Map<String, String>> infoTable = new ArrayList<>();
            infoTable.add(StringTable.stringMatrixToMap(new String[][]{
                    {"initializationTime", initializationTime},
                    {"size", size}
            }));
            return infoTable;
        }
    }


}
