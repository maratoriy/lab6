package application.controller.server.handlers;

import application.controller.commands.exceptions.NoSuchCommandException;
import application.controller.server.client.ServerClient;
import application.controller.server.messages.ClientMessage;
import application.controller.server.messages.Message;
import application.model.collection.CollectionItem;
import application.model.collection.CollectionManager;
import application.model.collection.exceptions.CollectionException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CommandHandler<T extends CollectionItem> extends PipeTypeAction {
//    public final CollectionManager<T> collectionManager;

    public CommandHandler(CollectionManager<T> collectionManager) {
        super(Message.Type.COMMAND);
//        this.collectionManager = collectionManager;

        commandProcessor = new HashMap<>();

        commandProcessor.put("clear", (message -> {
            collectionManager.clear();
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
        commandProcessor.put("updateById", (message -> {
            Long id = (Long) message.get("id");
            T item = collectionManager.getElemsClass().cast(message.get("item"));
            item.setupValueTree();
            collectionManager.updateById(id, item);
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
        commandProcessor.put("generateNew", (message -> {
            T item = collectionManager.generateNew();
//            item.setUser(message.login);
            return new Message(Message.Type.SUCCESS)
                    .put("item", item);
        }
        ));

        commandProcessor.put("add", (message ->
        {
            T item = collectionManager.getElemsClass().cast(message.get("element"));
            item.setupValueTree();
            collectionManager.add(item);
            return new Message(Message.Type.SUCCESS);
        }));
        commandProcessor.put("getById", (message ->

        {
            Long id = (Long) message.get("id");
            return new Message(Message.Type.SUCCESS)
                    .put("item", collectionManager.getById(id));
        }));
        commandProcessor.put("removeById", (message ->

        {
            Long id = (Long) message.get("id");
            return new Message(Message.Type.SUCCESS)
                    .put("removed", collectionManager.removeById(id));
        }));
    }

    protected final Map<String, Function<ClientMessage, Message>> commandProcessor;


    @Override
    protected void handleAction(ServerClient client, ClientMessage message) {
        try {
            String commandName = (String) message.get("commandName");
            if (!commandProcessor.containsKey(commandName)) throw new NoSuchCommandException();
            client.sendObject(commandProcessor.get(commandName).apply(message));
        } catch (CollectionException e) {
            client.sendObject(Message.error(e));
        }
    }
}
