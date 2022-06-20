package application.controller.server.handlers;

import application.controller.server.client.ServerClient;
import application.controller.server.messages.ClientMessage;
import application.controller.server.messages.Message;

abstract public class PipeTypeAction extends AbstractMessageHandler {
    private final Message.Type type;

    public PipeTypeAction(Message.Type type) {
        this.type = type;
    }

    @Override
    public void handleObject(ServerClient client, ClientMessage message) {
        if (message.getType() == type)
            handleAction(client, message);
        else if (next != null)
            next.handleObject(client, message);
    }

}