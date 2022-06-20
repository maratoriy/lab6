package application.model.collection.server;

import application.controller.exceptions.CriticalErrorException;
import application.controller.server.AuthorisationManager;
import application.controller.server.TCPCommunicator;
import application.controller.server.exceptions.ServerException;
import application.controller.server.handlers.AuthorizationHandler;
import application.controller.server.messages.ClientMessage;
import application.controller.server.messages.Message;
import application.model.collection.AbstractCollectionManager;
import application.model.collection.CollectionManager;
import application.view.datamodels.StringTable;

import java.io.IOException;
import java.util.List;

abstract public class TCPClientCollectionManager<T extends UserItem>
        implements CollectionManager<T>, AuthorisationManager {
    private final TCPCommunicator tcpCommunicator;

    public TCPClientCollectionManager(String hostName, int port) {
        this.tcpCommunicator = new TCPCommunicator(hostName, port);
    }

    private String login = "login";
    private String password = "default";

    private int triesToConnect = 0;

    @Override
    public void init() {
        if (isInit()) return;
        try {
            Thread.sleep(1000);
            tcpCommunicator.connect();
            triesToConnect = 0;
        } catch (IOException | InterruptedException e) {
            if (triesToConnect++ > 5) throw new CriticalErrorException("Couldn't connect to the server");
            throw new ServerException(e);
        }
    }

    public ClientMessage createConcreteMessage(Message.Type type) {
        return new ClientMessage(type, login, password);
    }

    @Override
    public Message login(String login, String password) {
        init();
        this.login = login;
        this.password = AuthorizationHandler.hashPassword(password, "default");
        return send(createConcreteMessage(Message.Type.AUTH)
                .put("action", "login")
        );
    }

    @Override
    public Message register(String login, String password) {
        init();
        this.login = login;
        this.password = AuthorizationHandler.hashPassword(password, "default");
        return send(createConcreteMessage(Message.Type.AUTH)
                .put("action", "register")
        );
    }

    private ClientMessage createCommand(String commandName) {
        return createConcreteMessage(Message.Type.COMMAND)
                .put("commandName", commandName);
    }

    synchronized private Message send(ClientMessage command) {
        init();
        tcpCommunicator.write(command);
        Message respond = tcpCommunicator.read();
        if (respond.getType() == Message.Type.ERROR) {
            throw (RuntimeException) respond.get("error");
        }
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
        T item = getElemsClass().cast(send(createCommand("generateNew")).get("item"));
        item.setupValueTree();
        return item;
    }

    @Override
    public int size() {
        return (Integer) send(createCommand("size")).get("size");
    }

    @Override
    public void clear() {
        send(createCommand("clear"));
    }

    @Override
    public void reverse() {
        send(createCommand("reverse"));
    }

    @Override
    public void sort() {
        send(createCommand("sort"));
    }

    @Override
    public void insertAtIndex(Integer index, T item) {
        send(createCommand("insertAtIndex")
                .put("index", index)
                .put("item", item)
        );
    }

    @Override
    public void updateById(Long id, T item) {
        send(createCommand("updateById")
                .put("id", id)
                .put("item", item)
        );
    }

    @Override
    public long countByValue(String valueName, String value) {
        return (Long) send(createCommand("countByValue")
                .put("valueName", valueName)
                .put("value", value)
        ).get("count");
    }

    @Override
    public void add(T element) {
        send(createCommand("add")
                .put("element", element));
    }


    @Override
    public T getById(Long id) {
        T item = getElemsClass().cast(
                send(
                        createCommand("getById")
                                .put("id", id)
                ).get("item"));
        item.setupValueTree();
        return item;
    }

    @Override
    public boolean removeById(Long id) {
        return (boolean) send(createCommand("removeById").put("id", id)).get("removed");
    }

    @Override
    public StringTable getCollectionTable() {
        return AbstractCollectionManager.getCollectionTable(asList());
    }

    @Override
    public StringTable getCollectionInfo() {
        return (StringTable) send(createCommand("getCollectionInfo"))
                .get("collectionInfo");
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> asFilteredList(CollectionFilter<? super T> predicate) {
        return (List<T>) send(createCommand("asFilteredList")
                .put("predicate", predicate))
                .get("list");
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> asList() {
        List<T> list = (List<T>) send(createCommand("asList"))
                .get("list");
        list.forEach(T::setupValueTree);
        return list;
    }
}
