package application.model.collection.server;

import application.model.collection.CollectionItem;

public interface UserItem extends CollectionItem {
    void setUser(String name);

    String getUser();
}
