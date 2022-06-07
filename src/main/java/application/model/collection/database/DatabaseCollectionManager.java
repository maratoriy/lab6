package application.model.collection.database;

import application.controller.server.exceptions.ServerException;
import application.model.collection.CollectionItem;
import application.model.collection.CollectionManager;
import application.model.collection.exceptions.CollectionException;
import application.model.collection.exceptions.NoSuchElemException;
import application.view.StringTable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseCollectionManager<T extends CollectionItem> implements CollectionManager<T> {
    private final CollectionManager<T> wrappedCollectionManager;
    private final Database database;
    static public String dataBaseName = "collection";
    {
        database = Database.getInstance();
    }

    public void parse() {
        try {
            ResultSet resultSet = database.executeQuery("SELECT * FROM " + dataBaseName);
            while(resultSet.next()) {
                T item = generateNew();
                item.parse(resultSet);
                wrappedCollectionManager.add(item);
            }
        } catch (SQLException e) {
            throw new ServerException(e);
        }
    }

    public DatabaseCollectionManager(CollectionManager<T> wrappedCollectionManager) {
        this.wrappedCollectionManager = wrappedCollectionManager;
    }

    private static DBRequest selectByIdDB(String user, Long id) {
        String sql = "SELECT * FROM "+dataBaseName+" WHERE \"user\" = ? AND \"id\" = ?";
        return new DBRequest(sql, user, id);
    }

    synchronized public boolean removeById(String user, Long id) {
        try {
            ResultSet resultSet = database.executeQuery(selectByIdDB(user, id));
            if(!resultSet.next()) throw new NoSuchElemException();
            return wrappedCollectionManager.removeById(id);
        } catch (SQLException e) {
            throw new ServerException(e);
        }
    }

    synchronized public void updateById(String user, Long id, T item) {
        try {
            ResultSet resultSet = database.executeQuery(selectByIdDB(user, id));
            if(!resultSet.next()) throw new NoSuchElemException();
            List<DBRequest> dbRequests = item.update(dataBaseName, true);
            doRequests(dbRequests);
            wrappedCollectionManager.updateById(id, item);
        } catch (SQLException e) {
            throw new ServerException(e);
        }

    }


    synchronized public void clear(String user) {
        try {
            List<Long> idsToDelete = new ArrayList<>();
            ResultSet resultSet = database.executeQuery(DBPerformable.selectAllDB(dataBaseName, user));
            while(resultSet.next()) {
                idsToDelete.add(resultSet.getLong("id"));
            }
            if (idsToDelete.size()>0) {
                List<DBRequest> dbRequests = wrappedCollectionManager.getById(idsToDelete.get(0)).deleteAll(dataBaseName, user);
                for (DBRequest iter:
                     dbRequests) {
                    database.executeUpdate(iter);
                }
                for (Long id:
                     idsToDelete) {
                    wrappedCollectionManager.removeById(id);
                }
            }
        } catch (SQLException e) {
            throw new CollectionException(e);
        }
    }

    @Override
    public int size() {
        return wrappedCollectionManager.size();
    }

    private void doRequests(List<DBRequest> dbRequests) throws SQLException {
        for (DBRequest iter:
                dbRequests) {
            database.executeUpdate(iter);
        }
    }

    @Override
    public void clear() {
        try {
            List<DBRequest> dbRequests = generateNew().deleteAllCompletely(dataBaseName);
            doRequests(dbRequests);
            wrappedCollectionManager.clear();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ServerException(e);
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
    public void insertAtIndex(Long index, T item) {
        wrappedCollectionManager.insertAtIndex(index, item);
    }

    @Override
    public void updateById(Long id, T item) {
        try {
            List<DBRequest> dbRequests = item.update(dataBaseName, true);
            doRequests(dbRequests);
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

    @Override
    synchronized public void add(T item) {
        try {
            List<DBRequest> dbRequests = item.insert(dataBaseName);

            doRequests(dbRequests);
            wrappedCollectionManager.add(item);
        } catch (SQLException e) {
            throw new ServerException(e);
        }
    }

    @Override
    public Class<T> getElemsClass() {
        return wrappedCollectionManager.getElemsClass();
    }

    @Override
    public T generateNew() {
        return wrappedCollectionManager.generateNew();
    }

    @Override
    public T getById(Long id) {
        return wrappedCollectionManager.getById(id);
    }

    @Override
    public boolean removeById(Long id) {
        try {
            doRequests(getById(id).delete(dataBaseName, false));
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
