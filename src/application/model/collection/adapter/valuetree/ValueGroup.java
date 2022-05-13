package application.model.collection.adapter.valuetree;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ValueGroup extends ValueNode implements Iterable<ValueNode> {
    private final NotNullChecker isNull;
    private final ValueGenerator generator;
    private final LinkedHashMap<String, ValueNode> valueNodes;

    @FunctionalInterface
    public interface NotNullChecker extends Supplier<Boolean>, Serializable {
    }

    {
        valueNodes = new LinkedHashMap<>();
    }

    public ValueGroup(String name) {
        super(name, NodeType.GROUP);
        this.generator = () -> {
        };
        this.isNull = () -> false;
    }

    public ValueGroup(String name, NotNullChecker isNull, ValueGenerator generator) {
        super(name, NodeType.GROUP);
        this.isNull = isNull;
        this.generator = generator;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addValueNode(ValueNode valueNode) {
        valueNodes.put(valueNode.getName(), valueNode);
        if (isBlocked()) valueNode.block();
        valueNode.setParent(this);
    }

    public boolean isGenerated() {
        return !isNull.get();
    }

    public void isNullGenerate() {
        if (isNull.get()) {
            generator.generate();
            if (parent != null) parent.isNullGenerate();
        }
    }

    @Override
    public Iterator<ValueNode> iterator() {
        return new Iterator<ValueNode>() {
            final Iterator<ValueNode> defaultIterator = valueNodes.values().iterator();
            Iterator<ValueNode> currentIterator = defaultIterator;

            @Override
            public boolean hasNext() {
                if (defaultIterator == currentIterator) return defaultIterator.hasNext();
                else if (currentIterator.hasNext()) return true;
                else {
                    currentIterator = defaultIterator;
                    return currentIterator.hasNext();
                }
            }

            @Override
            public ValueNode next() {
                ValueNode node = currentIterator.next();
                if (node.getType() == NodeType.GROUP && ((ValueGroup) node).isGenerated())
                    currentIterator = ((ValueGroup) node).iterator();
                return node;
            }
        };
    }

    public Optional<ValueNode> findValueNode(String fullName) {
        ValueNode valueNode = null;
        for (ValueNode iter : this) {
            if (iter.fullName().equals(fullName)) {
                valueNode = iter;
                break;
            }
        }
        return Optional.ofNullable(valueNode);
    }


    @Override
    public void forEach(Consumer<? super ValueNode> action) {
        for (ValueNode iter : this) {
            action.accept(iter);
        }
    }

    @Override
    public Spliterator<ValueNode> spliterator() {
        throw new UnsupportedOperationException();
    }


    @Override
    public ValueGroup block() {
        forEach(ValueNode::block);
        super.block();
        return this;
    }

    @Override
    public Element parse(Document document) {
        Element group = document.createElement(getName());
        valueNodes.values().forEach(node -> {
            if (node.getType() == NodeType.VALUE || (((ValueGroup) node).isGenerated()))
                group.appendChild(node.parse(document));
        });
        return group;
    }

    @Override
    public void parse(Element element) {
        NodeList nodeList = element.getChildNodes();
        Map<String, String> resultNull = valueNodes.keySet().stream().filter(key -> valueNodes.get(key).getType() == NodeType.VALUE).collect(
                Collectors.toMap(Function.identity(), item -> "")
        );
        Map<String, Value> valueMap = valueNodes.values().stream().filter(iter -> iter.getType() == NodeType.VALUE).collect(
                Collectors.toMap(ValueNode::getName, item -> (Value) item)
        );
        Map<String, ValueGroup> groupMap = valueNodes.values().stream().filter(iter -> iter.getType() == NodeType.GROUP).collect(
                Collectors.toMap(ValueNode::getName, item -> (ValueGroup) item)
        );
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            String nodeName = node.getNodeName();
            if (resultNull.containsKey(nodeName)) {
                resultNull.put(nodeName, node.getTextContent());
            } else if (groupMap.containsKey(nodeName) && node.getNodeType() == Node.ELEMENT_NODE)
                groupMap.get(nodeName).parse((Element) node);
        }
        resultNull.forEach((valueName, value) -> valueMap.get(valueName).setValue(value.equals("") ? null : value));
    }
}
