package application.controller.server.handlers;

import application.controller.server.client.ServerClient;
import application.controller.server.messages.ClientMessage;
import application.controller.server.messages.Message;

public class PartitionHandler extends PipeTypeAction {
    public PartitionHandler() {
        super(Message.Type.PARTS);
    }

    @Override
    protected void handleAction(ServerClient client, ClientMessage message) {
        client.startReceivingParts((Integer) message.get("parts"));
    }
}
