package application.controller.server.handlers;

import application.controller.server.Message;
import application.controller.server.client.ServerClient;

abstract public class AbstractMessageHandler implements MessageHandler {
    protected AbstractMessageHandler next;
    private Message.Type type;

    public AbstractMessageHandler() {
    }

    public AbstractMessageHandler(Message.Type type) {
        this.type = type;
    }

    public AbstractMessageHandler addNext(AbstractMessageHandler next) {
        if (this.next != null)
            this.next.addNext(next);
        else this.next = next;
        return this;
    }

    public void setType(Message.Type type) {
        this.type = type;
    }

    public Message.Type getType() {
        return type;
    }

    @Override
    public void handleObject(ServerClient client, Message message) {
        if (message.getType() == type)
            handleAction(client, message);
        else if (next != null)
            next.handleObject(client, message);
    }

    abstract protected void handleAction(ServerClient client, Message message);
}
