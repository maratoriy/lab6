package application.controller.server.handlers;

import application.controller.server.messages.Message;
import application.model.collection.server.UserCollectionManager;
import application.model.collection.server.UserItem;

public class UserCommandHandler<T extends UserItem> extends CommandHandler<T> {

    public UserCommandHandler(UserCollectionManager<T> collectionManager) {
        super(collectionManager);

        commandProcessor.put("clear", (message -> {
            collectionManager.clear(message.login);
            return new Message(Message.Type.SUCCESS);
        }));
        commandProcessor.put("updateById", (message -> {
            Long id = (Long) message.get("id");
            T item = collectionManager.getElemsClass().cast(message.get("item"));
            item.setupValueTree();
            collectionManager.updateById(message.login, id, item);
            return new Message(Message.Type.SUCCESS);
        }));
        commandProcessor.put("generateNew", (message -> {
            T item = collectionManager.generateNew();
            item.setUser(message.login);
            return new Message(Message.Type.SUCCESS)
                    .put("item", item);
        }
        ));
        commandProcessor.put("removeById", (message ->
        {
            Long id = (Long) message.get("id");
            return new Message(Message.Type.SUCCESS)
                    .put("removed", collectionManager.removeById(message.login, id));
        }));
    }
}
