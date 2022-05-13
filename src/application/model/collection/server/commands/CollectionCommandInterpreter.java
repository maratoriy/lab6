package application.model.collection.server.commands;

import application.controller.server.client.ClientTask;
import application.controller.server.client.ServerClient;
import application.controller.server.handlers.AbstractObjectHandler;
import application.controller.server.handlers.ObjectHandler;
import application.model.collection.CollectionItem;
import application.model.collection.CollectionManager;
import application.model.collection.exceptions.CollectionException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectionCommandInterpreter<T extends CollectionItem> extends AbstractObjectHandler<CollectionCommand> {
    private final Map<String, ObjectHandler<CollectionCommand>> commandMap;
    private final CollectionManager<T> collectionManager;

    {
        commandMap = new HashMap<>();
    }

    public CollectionCommandInterpreter(CollectionManager<T> collectionManager) {
        super(CollectionCommand.class);
        this.collectionManager = collectionManager;

        commandMap.put("size()", (((client, object) -> client.pushBackTask(ClientTask.writeTask(collectionManager.size())))));
        commandMap.put("clear()", (((client, object) -> collectionManager.clear())));
        commandMap.put("reverse()", (((client, object) -> collectionManager.reverse())));
        commandMap.put("sort()", (((client, object) -> collectionManager.sort())));
        commandMap.put("asList()", ((client, object) -> {
            List<T> tList = collectionManager.asList();
            client.pushBackTask(ClientTask.writeTask(tList.size()));
            tList.forEach(item -> client.pushBackTask(ClientTask.writeTask(item)));
        }));
        commandMap.put("add()", (((client, object) -> collectionManager.add(collectionManager.getElemsClass().cast(object.get("element"))))));
        commandMap.put("generateNew()", ((client, object) -> client.pushBackTask(ClientTask.writeTask(collectionManager.generateNew()))));
        commandMap.put("getCollectionInfo()", (((client, object) -> client.pushBackTask(ClientTask.writeTask(collectionManager.getCollectionInfo())))));
        commandMap.put("getById()", (((client, object) -> client.pushBackTask(ClientTask.writeTask(collectionManager.getById((Long) object.get("id")))))));
        commandMap.put("removeById()", (((client, object) -> client.pushBackTask(ClientTask.writeTask(collectionManager.removeById((Long) object.get("id")))))));
        commandMap.put("countByValue()", ((client, object) -> client.pushBackTask(ClientTask.writeTask(collectionManager.countByValue(
                (String) object.get("valueName"),
                (String) object.get("value")
        )))));
        commandMap.put("updateById()", (((client, object) -> collectionManager.updateById((Long) object.get("id"), collectionManager.getElemsClass().cast(object.get("item"))))));
        commandMap.put("insertAtIndex()", ((client, object) -> collectionManager.insertAtIndex((Integer) object.get("index"), collectionManager.getElemsClass().cast(object.get("item")))));
        commandMap.put("asFilteredList()", (((client, object) -> {
            List<T> tList = collectionManager.asFilteredList((CollectionManager.CollectionFilter<? super T>) object.get("predicate"));
            client.pushBackTask(ClientTask.writeTask(tList.size()));
            collectionManager.asList().forEach(item -> client.pushBackTask(ClientTask.writeTask(item)));
        })));
    }

    @Override
    public void action(ServerClient client, CollectionCommand object) {
        try {
            synchronized (collectionManager) {
                if (commandMap.containsKey(object.getName())) {
                    client.clearTaskSet();
                    commandMap.get(object.getName()).handleObject(client, object);
                } else throw new CollectionException("No such command on server!");
            }
        } catch (RuntimeException e) {
            client.clearTaskSet();
            client.pushForwardTask(ClientTask.writeTask(e));
        }
    }
}
