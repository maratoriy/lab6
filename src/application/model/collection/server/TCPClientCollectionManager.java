package application.model.collection.server;

import application.controller.exceptions.CriticalErrorException;
import application.controller.server.TCPCommunicator;
import application.controller.server.client.ClientTask;
import application.controller.server.client.ServerClient;
import application.controller.server.exceptions.ServerException;
import application.controller.server.handlers.ClientTaskHandler;
import application.controller.server.handlers.ExceptionHandler;
import application.controller.view.StringTable;
import application.model.collection.AbstractCollectionManager;
import application.model.collection.CollectionItem;
import application.model.collection.CollectionManager;
import application.model.collection.server.commands.CollectionCommand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

abstract public class TCPClientCollectionManager<T extends CollectionItem> implements CollectionManager<T> {
    private final TCPCommunicator tcpCommunicator;
    private ServerClient appClient;

    public TCPClientCollectionManager(String hostName, int port) {
        this.tcpCommunicator = new TCPCommunicator(hostName, port);
        tcpCommunicator.getFirstHandler()
                .addNext(new ExceptionHandler())
                .addNext(new ClientTaskHandler());
    }

    private int triesToConnect = 0;

    @Override
    public void init() {
        try {
            tcpCommunicator.connect();
            appClient = tcpCommunicator.getAppClient();
            triesToConnect = 0;
        } catch (IOException e ) {
            if (triesToConnect++ > 5) throw new CriticalErrorException("Couldn't connect to the server");
            throw new ServerException(e);
        }
    }

    @Override
    public boolean isInit() {
        return tcpCommunicator.isRunning();
    }

    @Override
    public void close() {
        tcpCommunicator.close();
    }

    @Override
    public int size() {
        appClient.pushBackTask(ClientTask.writeTask(new CollectionCommand("size()")));
        Wrapper<Integer> integerWrapper = new Wrapper<>();
        appClient.pushBackTask(ClientTask.readTask(Integer.class, integerWrapper::setObject));
        tcpCommunicator.run();
        return integerWrapper.getObject();
    }

    @Override
    public void clear() {
        appClient.pushBackTask(ClientTask.writeTask(new CollectionCommand("clear()")));
        tcpCommunicator.run();
    }

    @Override
    public void reverse() {
        appClient.pushBackTask(ClientTask.writeTask(new CollectionCommand("reverse()")));
        tcpCommunicator.run();
    }

    @Override
    public void sort() {
        appClient.pushBackTask(ClientTask.writeTask(new CollectionCommand("sort()")));
        tcpCommunicator.run();
    }

    @Override
    public void insertAtIndex(int index, T item) {
        appClient.pushBackTask(ClientTask.writeTask(new CollectionCommand("insertAtIndex()")
                .put("index", index)
                .put("item", item)));
        tcpCommunicator.run();
    }

    @Override
    public void updateById(Long id, T item) {
        appClient.pushBackTask(ClientTask.writeTask(new CollectionCommand("updateById()")
                .put("id", id)
                .put("item", item)));
        tcpCommunicator.run();
    }

    @Override
    public long countByValue(String valueName, String value) {
        appClient.pushBackTask(ClientTask.writeTask(new CollectionCommand("countByValue()")
                .put("valueName", valueName)
                .put("value", value)));
        Wrapper<Long> wrapper = new Wrapper<>();
        appClient.pushBackTask(ClientTask.readTask(Long.class, wrapper::setObject));
        tcpCommunicator.run();
        return wrapper.getObject();
    }

    @Override
    public void add(T element) {
        appClient.pushBackTask(ClientTask.writeTask(new CollectionCommand("add()")
                .put("element", element)));
        appClient.pushBackTask(ClientTask.writeTask(element));
        tcpCommunicator.run();
    }

    @Override
    public T generateNew() {
        Wrapper<T> wrapper = new Wrapper<>();
        appClient.pushBackTask(ClientTask.writeTask(new CollectionCommand("generateNew()")));
        appClient.pushBackTask(ClientTask.readTask(getElemsClass(), wrapper::setObject));
        tcpCommunicator.run();
        return wrapper.getObject();
    }

    @Override
    public T getById(Long id) {
        appClient.pushBackTask(ClientTask.writeTask(new CollectionCommand("getById()")
                .put("id", id)));
        Wrapper<T> tWrapper = new Wrapper<>();
        appClient.pushBackTask(ClientTask.readTask(getElemsClass(), tWrapper::setObject));
        tcpCommunicator.run();
        return tWrapper.getObject();
    }

    @Override
    public boolean removeById(Long id) {
        appClient.pushBackTask(ClientTask.writeTask(new CollectionCommand("removeById()")
                .put("id", id)));
        Wrapper<Boolean> booleanWrapper = new Wrapper<>();
        appClient.pushBackTask(ClientTask.readTask(Boolean.class, booleanWrapper::setObject));
        tcpCommunicator.run();
        return booleanWrapper.getObject();
    }

    @Override
    public StringTable getCollectionTable() {
        return AbstractCollectionManager.getCollectionTable(asList());
    }

    @Override
    public StringTable getCollectionInfo() {
        appClient.pushBackTask(ClientTask.writeTask(new CollectionCommand("getCollectionInfo()")));
        Wrapper<StringTable> tableWrapper = new Wrapper<>();
        appClient.pushBackTask(ClientTask.readTask(StringTable.class, tableWrapper::setObject));
        tcpCommunicator.run();
        return tableWrapper.getObject();
    }

    @Override
    public List<T> asFilteredList(CollectionFilter<? super T> predicate) {
        appClient.pushBackTask(ClientTask.writeTask(new CollectionCommand("asFilteredList()")
                .put("predicate", predicate)));
        Wrapper<Integer> integerWrapper = new Wrapper<>();
        appClient.pushBackTask(ClientTask.readTask(Integer.class, integerWrapper::setObject));
        tcpCommunicator.run();
        int size = integerWrapper.getObject();
        List<T> tList = new ArrayList<>();
        while (size-- > 0) appClient.pushBackTask(new ClientTask.ReadTask<>(getElemsClass(), tList::add));
        tcpCommunicator.run();
        return tList;
    }

    @Override
    public List<T> asList() {
        int size = size();
        appClient.pushBackTask(ClientTask.writeTask(new CollectionCommand("asList()")));
        List<T> tList = new ArrayList<>();
        while (size-- >= 0) appClient.pushBackTask(new ClientTask.ReadTask<>(getElemsClass(), tList::add));
        tcpCommunicator.run();
        return tList;
    }

static final private class Wrapper<T> {
    private T object;

    public void setObject(T object) {
        this.object = object;
    }

    public T getObject() {
        return object;
    }
}
}
