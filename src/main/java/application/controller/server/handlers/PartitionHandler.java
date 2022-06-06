package application.controller.server.handlers;

import application.controller.server.Message;
import application.controller.server.client.ServerClient;

public class PartitionHandler extends AbstractMessageHandler {
    public PartitionHandler() {
        super(Message.Type.PARTS);
    }

    @Override
    protected void handleAction(ServerClient client, Message message) {
        client.startReceivingParts((Integer) message.get("parts"));
    }
}
