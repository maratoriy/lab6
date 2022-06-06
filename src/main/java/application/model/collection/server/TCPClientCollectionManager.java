package application.model.collection.server;

import application.controller.exceptions.CriticalErrorException;
import application.controller.server.Message;
import application.controller.server.TCPCommunicator;
import application.controller.server.exceptions.ServerException;
import application.model.collection.AbstractCollectionManager;
import application.model.collection.CollectionItem;
import application.model.collection.CollectionManager;
import application.view.StringTable;

import java.io.IOException;
import java.util.List;

abstract public class TCPClientCollectionManager<T extends CollectionItem> implements CollectionManager<T> {
    private final TCPCommunicator tcpCommunicator;

    public TCPClientCollectionManager(String hostName, int port) {
        this.tcpCommunicator = new TCPCommunicator(hostName, port);
    }

    private int triesToConnect = 0;

    @Override
    public void init() {
        try {
            Thread.sleep(1000);
            tcpCommunicator.connect();
            triesToConnect = 0;
        } catch (IOException | InterruptedException e) {
            if (triesToConnect++ > 5) throw new CriticalErrorException("Couldn't connect to the server");
            throw new ServerException(e);
        }
    }

    static private Message createCommand(String commandName) {
        return new Message(Message.Type.COMMAND)
                .put("commandName", commandName);
    }

    private Message sendCommand(Message command) {
        tcpCommunicator.write(command);
        Message respond = tcpCommunicator.read();
        if (respond.getType() == Message.Type.ERROR)
            throw (RuntimeException) respond.get("error");
        return respond;
    }

    @Override
    public boolean isInit() {
        return tcpCommunicator.isConnected();
    }

    @Override
    public void close() {
        tcpCommunicator.close();
    }

    @Override
    public T generateNew() {
        return getElemsClass().cast(sendCommand(createCommand("generateNew")).get("item"));
    }

    @Override
    public int size() {
        return (Integer) sendCommand(createCommand("size")).get("size");
    }

    @Override
    public void clear() {
        sendCommand(createCommand("clear"));
    }

    @Override
    public void reverse() {
        sendCommand(createCommand("reverse"));
    }

    @Override
    public void sort() {
        sendCommand(createCommand("sort"));
    }

    @Override
    public void insertAtIndex(int index, T item) {
        sendCommand(createCommand("insertAtIndex")
                .put("index", index)
                .put("item", item)
        );
    }

    @Override
    public void updateById(Long id, T item) {
        sendCommand(createCommand("updateById")
                .put("id", id)
                .put("item", item)
        );
    }

    @Override
    public long countByValue(String valueName, String value) {
        return (Long) sendCommand(createCommand("countByValue")
                .put("valueName", valueName)
                .put("value", value)
        ).get("count");
    }

    @Override
    public void add(T element) {
        sendCommand(createCommand("add")
                .put("element", element));
    }


    @Override
    public T getById(Long id) {
        return getElemsClass().cast(
                sendCommand(
                        createCommand("getById")
                                .put("id", id)
                ).get("item"));
    }

    @Override
    public boolean removeById(Long id) {
        return (boolean) sendCommand(createCommand("removeById").put("id", id)).get("removed");
    }

    @Override
    public StringTable getCollectionTable() {
        return AbstractCollectionManager.getCollectionTable(asList());
    }

    @Override
    public StringTable getCollectionInfo() {
        return (StringTable) sendCommand(createCommand("getCollectionInfo"))
                .get("collectionInfo");
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> asFilteredList(CollectionFilter<? super T> predicate) {
        return (List<T>) sendCommand(createCommand("asFilteredList")
                .put("predicate", predicate))
                .get("list");
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> asList() {
        return (List<T>) sendCommand(createCommand("asList"))
                .get("list");
    }
}
