package application.model.collection.adapter.valuetree;

import application.model.parse.DomParseable;

import java.io.Serializable;

abstract public class ValueNode implements DomParseable, Serializable {
    protected String name;
    protected ValueGroup parent;
    private final NodeType nodeType;
    private boolean blocked = false;

    protected ValueNode(String name, NodeType nodeType) {
        this.name = name;
        this.nodeType = nodeType;
    }

    public void setParent(ValueGroup parent) {
        this.parent = parent;
    }

    public NodeType getType() {
        return nodeType;
    }

    public String getName() {
        return name;
    }

    public String fullName() {
        return (parent != null) ? parent.fullName() + "." + getName() : getName();
    }

    public ValueNode block() {
        blocked = true;
        return this;
    }

    public boolean isBlocked() {
        return blocked;
    }


    public enum NodeType {
        VALUE,
        GROUP
    }

}
