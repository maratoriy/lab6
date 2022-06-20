package application.controller.server.handlers;

import application.controller.server.client.ServerClient;
import application.controller.server.messages.ClientMessage;
import application.controller.server.messages.Message;

public class DataFilter extends PipeTypeAction {
    public DataFilter() {
        super(Message.Type.DATA);
    }

    @Override
    protected void handleAction(ServerClient client, ClientMessage message) {
        client.receiveObject(message);
    }
}
