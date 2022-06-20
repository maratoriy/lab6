package application.model.collection;

import application.model.parse.DomParseable;

import java.io.Serializable;
import java.util.List;

public interface CollectionItem extends Comparable<CollectionItem>, DomParseable, Serializable {
    Long getId();


    void setId(Long id);


    void generateField(String valueName);

    String getValue(String valueName);

    void setValue(String valueName, String value);

    List<String> getNullGroupsNames();

    List<String> getGettersNames();

    List<String> getSettersNames();

    List<String> getUniqueFields();

    void setupValueTree();


    @Override
    default int compareTo(CollectionItem o) {
        return getId().compareTo(o.getId());
    }

}
