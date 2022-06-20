package application.model.collection.database;

import application.controller.server.exceptions.ServerException;
import application.model.collection.AbstractCollectionManager;
import application.model.collection.CollectionManager;
import application.model.collection.exceptions.CollectionException;
import application.model.collection.exceptions.InvalidUserException;
import application.model.collection.server.UserCollectionManager;
import application.view.datamodels.StringTable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class DatabaseCollectionManager<T extends DBCollectionItem> implements UserCollectionManager<T> {
    private final AbstractCollectionManager<T> wrappedCollectionManager;
    private final Database database;
    public String dataBaseName = "collection";
    static public final String admin = "admin";

    {
        database = Database.getInstance();
    }

    public void parse() {
        try {
            ResultSet infoSet = database.executeQuery("SELECT * from \"collectionInfo\"");
            if(infoSet.next()) wrappedCollectionManager.setInitializationTime(LocalDateTime.parse(infoSet.getString("initializationTime")));
            else {
                database.executeUpdate("INSERT INTO \"collectionInfo\" VALUES(?)", wrappedCollectionManager.getInitializationTime().toString());
            }
            ResultSet resultSet = database.executeQuery("SELECT * FROM " + dataBaseName);
            while (resultSet.next()) {
                T item = generateNew();
                item.parse(resultSet);
                wrappedCollectionManager.add(item);
            }
        } catch (SQLException e) {
            throw new ServerException(e);
        }
    }

    public DatabaseCollectionManager(AbstractCollectionManager<T> wrappedCollectionManager) {
        this.wrappedCollectionManager = wrappedCollectionManager;
    }


    @Override
    public int size() {
        return wrappedCollectionManager.size();
    }

    @Override
    public void clear(String user) {
        try {
            List<T> filteredByUser = wrappedCollectionManager.asFilteredList(item -> item.getUser().equals(user));
            if (filteredByUser.size() == 0) return;
            T exampleItem = filteredByUser.stream().findAny().get();
            filteredByUser.forEach(item -> wrappedCollectionManager.removeById(item.getId()));
            database.executeUpdates(exampleItem.deleteAll(dataBaseName, user));
        } catch (SQLException e) {
            throw new CollectionException(e);
        }
    }

    @Override
    public void clear() {
        try {
            wrappedCollectionManager.clear();
            List<DBRequest> dbRequests = generateNew().deleteAllCompletely(dataBaseName);
            database.executeUpdates(dbRequests);
        } catch (SQLException e) {
            throw new CollectionException(e);
        }
    }

    @Override
    public void reverse() {
        wrappedCollectionManager.reverse();
    }

    @Override
    public void sort() {
        wrappedCollectionManager.sort();
    }

    @Override
    public void insertAtIndex(Integer index, T item) {
        insert(item);
        wrappedCollectionManager.insertAtIndex(index, item);
    }

    @Override
    public void updateById(String user, Long id, T item) {
        try {
            if (!getById(id).getUser().equals(user)) throw new InvalidUserException();
            List<DBRequest> dbRequests = item.update(dataBaseName);
            database.executeUpdates(dbRequests);
            wrappedCollectionManager.updateById(id, item);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ServerException(e);
        }
    }

    @Override
    public void updateById(Long id, T item) {
        try {
            List<DBRequest> dbRequests = item.update(dataBaseName);
            database.executeUpdates(dbRequests);
            wrappedCollectionManager.updateById(id, item);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ServerException(e);
        }
    }

    @Override
    public long countByValue(String valueName, String value) {
        return wrappedCollectionManager.countByValue(valueName, value);
    }

    private void insert(T item) {
        try {
            List<DBRequest> dbRequests = item.insert(dataBaseName);
            database.executeUpdates(dbRequests);
            ResultSet resultSet = database.executeQuery("SELECT currval('nextval')");
            resultSet.next();
            item.setId(resultSet.getLong("currval"));
        } catch (SQLException e) {
            throw new ServerException(e);
        }
    }

    @Override
    synchronized public void add(T item) {
        insert(item);
        wrappedCollectionManager.add(item);
    }

    @Override
    public Class<T> getElemsClass() {
        return wrappedCollectionManager.getElemsClass();
    }

    @Override
    public T generateNew() {
        T item = wrappedCollectionManager.generateNew();
        item.setUser(admin);
        return item;
    }

    @Override
    public T generateNew(String user) {
        T item = wrappedCollectionManager.generateNew();
        item.setUser(user);
        return item;
    }

    @Override
    public T getById(Long id) {
        return wrappedCollectionManager.getById(id);
    }

    @Override
    public boolean removeById(String user, Long id) {
        try {
            T item = getById(id);
            if (!item.getUser().equals(user)) throw new InvalidUserException();
            database.executeUpdates(getById(id).delete(dataBaseName));
            return wrappedCollectionManager.removeById(id);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ServerException(e);
        }
    }

    @Override
    public boolean removeById(Long id) {
        try {
            database.executeUpdates(getById(id).delete(dataBaseName));
            return wrappedCollectionManager.removeById(id);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ServerException(e);
        }
    }


    @Override
    public StringTable getCollectionTable() {
        return wrappedCollectionManager.getCollectionTable();
    }

    @Override
    public StringTable getCollectionInfo() {
        return wrappedCollectionManager.getCollectionInfo();
    }

    @Override
    public List<T> asFilteredList(CollectionFilter<? super T> predicate) {
        return wrappedCollectionManager.asFilteredList(predicate);
    }

    @Override
    public List<T> asList() {
        return wrappedCollectionManager.asList();
    }

}
