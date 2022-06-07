package application.model.collection.server;

import application.controller.server.TCPServer;
import application.controller.server.handlers.*;
import application.model.collection.AbstractCollectionManager;
import application.model.collection.CollectionItem;
import application.model.collection.CollectionManager;
import application.model.collection.database.DatabaseCollectionManager;
import application.view.StringTable;

import java.util.List;

public class TCPServerCollectionManager<T extends CollectionItem> implements CollectionManager<T> {
    private final DatabaseCollectionManager<T> wrappedCollectionManager;
    private final TCPServer server;
    private final Thread serverThread;

    public TCPServerCollectionManager(DatabaseCollectionManager<T> wrappedCollectionManager, String hostName, int port) {
        this.wrappedCollectionManager = wrappedCollectionManager;
        this.server = new TCPServer(hostName, port);

        MessageHandler messageHandlerChain = new PartitionHandler()
                .addNext(new DataFilter())
                .addNext(new NewStreamFilter(new AuthorizationHandler()))
                .addNext(new NewStreamFilter(new CommandHandler<>(wrappedCollectionManager)));

        server.setMessageHandler(messageHandlerChain);

        this.serverThread = new Thread(server::run);
        serverThread.setDaemon(true);
    }

    @Override
    public void close() {
        wrappedCollectionManager.close();
        server.close();
    }

    @Override
    public void init() {
        if(!wrappedCollectionManager.isInit()) wrappedCollectionManager.init();
        serverThread.start();
    }

    @Override
    public boolean isInit() {
        return wrappedCollectionManager.isInit()&&serverThread.isAlive();
    }

    @Override
    public int size() {
        return wrappedCollectionManager.size();
    }

    @Override
    public void clear() {
        wrappedCollectionManager.clear();
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
    public void insertAtIndex(Long id, T item) {
        wrappedCollectionManager.insertAtIndex(id, item);
    }

    @Override
    public void updateById(Long id, T item) {
        wrappedCollectionManager.updateById(id, item);
    }

    @Override
    public long countByValue(String valueName, String value) {
        return wrappedCollectionManager.countByValue(valueName, value);
    }

    @Override
    public void add(T element) {
        wrappedCollectionManager.add(element);
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
        return wrappedCollectionManager.removeById(id);
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
