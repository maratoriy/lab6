package application.model.collection.server;

import application.model.collection.CollectionManager;

public interface UserCollectionManager<T extends UserItem> extends CollectionManager<T> {
    void clear(String user);

    boolean removeById(String user, Long id);

    void updateById(String user, Long id, T item);

    T generateNew(String user);
}
