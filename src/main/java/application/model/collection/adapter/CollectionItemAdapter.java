package application.model.collection.adapter;

import application.model.collection.adapter.valuetree.Value;
import application.model.collection.adapter.valuetree.ValueGroup;
import application.model.collection.adapter.valuetree.ValueNode;
import application.model.collection.database.DBCollectionItem;
import application.model.collection.database.DBRequest;
import application.model.data.exceptions.NoSuchFieldException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

abstract public class CollectionItemAdapter<T> implements DBCollectionItem {
    protected transient ValueGroup valueGroup;
    private String user = "local";

    public CollectionItemAdapter(Long id) {
    }

    @Override
    public void setupValueTree() {
        valueGroup = new ValueGroup("collectionItem");
        valueGroup.addValueNode(new Value("id",
                () -> getId().toString(),
                (str) -> setId(Long.valueOf(str)))
                .block()
                .setUnique()
        );
        valueGroup.addValueNode(new Value("user",
                this::getUser,
                this::setUser)
                .block());
    }

    @Override
    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public List<String> getUniqueFields() {
        List<String> nameList = new ArrayList<>();
        valueGroup.forEach(node -> {
            if (node.getType() == ValueNode.NodeType.VALUE && ((Value) node).isUnique())
                nameList.add(node.fullName());
        });
        return nameList;
    }

    @Override
    public void generateField(String valueName) {
        ValueNode valueNode = valueGroup.findValueNode(valueName).orElseThrow(NoSuchFieldException::new);
        if (valueNode.getType() == ValueNode.NodeType.GROUP) {
            ((ValueGroup) valueNode).isNullGenerate();
        }
    }

    @Override
    public String getValue(String valueName) {
        ValueNode valueNode = valueGroup.findValueNode(valueName).orElseThrow(NoSuchFieldException::new);
        if (valueNode.getType() == ValueNode.NodeType.VALUE) {
            return ((Value) valueNode).getValue();
        } else {
            throw new NoSuchFieldException();
        }
    }

    @Override
    public void setValue(String valueName, String value) {
        ValueNode valueNode = valueGroup.findValueNode(valueName).orElseThrow(NoSuchFieldException::new);
        if (valueNode.getType() == ValueNode.NodeType.VALUE) {
            ((Value) valueNode).setValue(value);
        } else {
            throw new NoSuchFieldException();
        }
    }

    @Override
    public List<String> getNullGroupsNames() {
        List<String> nameList = new ArrayList<>();
        valueGroup.forEach(node -> {
            if (node.getType() == ValueNode.NodeType.GROUP && !((ValueGroup) node).isGenerated())
                nameList.add(node.fullName());
        });
        return nameList;
    }

    @Override
    public List<String> getGettersNames() {
        List<String> nameList = new ArrayList<>();
        valueGroup.forEach(node -> {
            if (node.getType() == ValueNode.NodeType.VALUE)
                nameList.add(node.fullName());
        });
        return nameList;
    }

    @Override
    public List<String> getSettersNames() {
        List<String> nameList = new ArrayList<>();
        valueGroup.forEach(node -> {
            if (!node.isBlocked() && node.getType() == ValueNode.NodeType.VALUE)
                nameList.add(node.fullName());
        });
        return nameList;
    }

    @Override
    public List<DBRequest> deleteAll(String db_name, String user) {
        List<DBRequest> deleteAll = new ArrayList<>();
        deleteAll.add(DBCollectionItem.deleteAllByUser(db_name, user));
        return deleteAll;
    }

    @Override
    public List<DBRequest> deleteAllCompletely(String db_name) {
        List<DBRequest> deleteAll = new ArrayList<>();
        deleteAll.add(DBCollectionItem.deleteAll(db_name));
        return deleteAll;
    }

    @Override
    public List<DBRequest> delete(String db_name) {
        List<DBRequest> dbRequests = new ArrayList<>();
        String where = "WHERE \"id\" = ?";
        String deleteWorker = "DELETE FROM " + db_name + " " + where;
        dbRequests.add(new DBRequest(deleteWorker, getId()));
        return dbRequests;
    }

    @Override
    public Element parse(Document document) {
        return valueGroup.parse(document);
    }

    @Override
    public void parse(Element element) {
        valueGroup.parse(element);
    }


}
