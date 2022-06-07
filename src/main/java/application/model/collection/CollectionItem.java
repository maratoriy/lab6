package application.model.collection;

import application.model.collection.database.DBPerformable;
import application.model.parse.DomParseable;

import java.io.Serializable;
import java.util.List;

public interface CollectionItem extends Comparable<CollectionItem>, DomParseable, Serializable, DBPerformable {
    Long getId();

    String getUser();
    void setUser(String user);

    void setId(Long id);


    void generateField(String valueName);

    String getValue(String valueName);

    void setValue(String valueName, String value);

    List<String> getNullGroupsNames();

    List<String> getGettersNames();

    List<String> getSettersNames();

    List<String> getUniqueFields();



    @Override
    default int compareTo(CollectionItem o) {
        return getId().compareTo(o.getId());
    }

}
