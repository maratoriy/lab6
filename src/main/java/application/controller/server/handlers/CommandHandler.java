package application.controller.server.handlers;

import application.controller.commands.exceptions.NoSuchCommandException;
import application.controller.server.Message;
import application.controller.server.TCPServer;
import application.controller.server.client.ServerClient;
import application.controller.server.exceptions.ServerException;
import application.model.collection.CollectionItem;
import application.model.collection.CollectionManager;
import application.model.collection.database.DatabaseCollectionManager;
import application.model.collection.exceptions.CollectionException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CommandHandler<T extends CollectionItem> extends AbstractMessageHandler {
    public final DatabaseCollectionManager<T> collectionManager;

    public CommandHandler(DatabaseCollectionManager<T> collectionManager) {
        super(Message.Type.COMMAND);
        this.collectionManager = collectionManager;

        commandProcessor = new HashMap<>();

        commandProcessor.put("clear", (message -> {
            String user = (String) message.get("login");
            collectionManager.clear(user);
            return new Message(Message.Type.SUCCESS);
        }));
        commandProcessor.put("size", (message ->
                new Message(Message.Type.SUCCESS)
                        .put("size", collectionManager.size())
        ));
        commandProcessor.put("reverse", (message -> {
            collectionManager.reverse();
            return new Message(Message.Type.SUCCESS);
        }));
        commandProcessor.put("sort", (message -> {
            collectionManager.sort();
            return new Message(Message.Type.SUCCESS);
        }));
        commandProcessor.put("insertAtIndex", (message -> {
            Integer index = (Integer) message.get("index");
            T item = collectionManager.getElemsClass().cast(message.get("item"));
            return new Message(Message.Type.SUCCESS);
        }));
        commandProcessor.put("updateById", (message -> {
            String user = (String) message.get("login");
            Long id = (Long) message.get("id");
            T item = collectionManager.getElemsClass().cast(message.get("item"));
            collectionManager.updateById(user, id, item);
            return new Message(Message.Type.SUCCESS);
        }));
        commandProcessor.put("countByValue", (message -> {
            String valueName = (String) message.get("valueName");
            String value = (String) message.get("value");
            return new Message(Message.Type.SUCCESS)
                    .put("count", collectionManager.countByValue(valueName, value));
        }));
        commandProcessor.put("getCollectionInfo", (message ->
                new Message(Message.Type.SUCCESS)
                        .put("collectionInfo", collectionManager.getCollectionInfo())
        ));
        commandProcessor.put("asList", (message ->
                new Message(Message.Type.SUCCESS)
                        .put("list", collectionManager.asList())
        ));
        commandProcessor.put("asFilteredList", (message ->
                new Message(Message.Type.SUCCESS)
                        .put("list", collectionManager.asFilteredList((CollectionManager.CollectionFilter<? super T>) message.get("predicate")))
        ));
        commandProcessor.put("generateNew", (message ->
            new Message(Message.Type.SUCCESS)
                    .put("item", collectionManager.generateNew())
        ));
        commandProcessor.put("add", (message -> {
            collectionManager.add(collectionManager.getElemsClass().cast(message.get("element")));
            return new Message(Message.Type.SUCCESS);
        }));
        commandProcessor.put("getById", (message -> {
            Long id = (Long) message.get("id");
            return new Message(Message.Type.SUCCESS)
                    .put("item", collectionManager.getById(id));
        }));
        commandProcessor.put("removeById", (message -> {
            String user = (String) message.get("login");
            Long id = (Long) message.get("id");
            return new Message(Message.Type.SUCCESS)
                    .put("removed", collectionManager.removeById(user, id));
        }));
    }

    private final Map<String, Function<Message, Message>> commandProcessor;


    @Override
    protected void handleAction(ServerClient client, Message message) {
        try {
            String commandName = (String) message.get("commandName");
            String login = (String) message.get("login");
            if (!commandProcessor.containsKey(commandName)) throw new NoSuchCommandException();
            client.sendObject(commandProcessor.get(commandName).apply(message));
        } catch (CollectionException e) {
            client.sendObject(Message.error(new ServerException(e.getMessage())));
        }
    }
}
