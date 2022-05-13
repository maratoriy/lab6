package application.controller.server.handlers;

import application.controller.server.TCPServer;
import application.controller.server.client.ClientTask;
import application.controller.server.client.ServerClient;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class CommunicatingHandler extends AbstractObjectHandler<CommunicatingHandler.Message> {
    private final Map<Message, ObjectHandler<Message>> messageHandlerMap;

    public CommunicatingHandler() {
        super(CommunicatingHandler.Message.class);
    }

    {
        messageHandlerMap = new HashMap<>();
        messageHandlerMap.put(Message.CANCEL, ((client, object) -> client.clearTaskSet()));
        messageHandlerMap.put(Message.STARTED, ((client, object) -> {
            TCPServer.log("\"%s\" started communicating", client.getName());
        }));
        messageHandlerMap.put(Message.COMPLETE, (((client, object) -> client.writeLock=false)));
    }


    @Override
    public void action(ServerClient client, Message object) {
        if(messageHandlerMap.containsKey(object))
            messageHandlerMap.get(object).handleObject(client, object);
    }

    public enum Message {
        STARTED,
        COMPLETE,
        CANCEL
    }
}
