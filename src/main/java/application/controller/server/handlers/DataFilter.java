package application.controller.server.handlers;

import application.controller.server.Message;
import application.controller.server.TCPServer;
import application.controller.server.client.ServerClient;

public class DataFilter extends AbstractMessageHandler {
    public DataFilter() {
        super(Message.Type.DATA);
    }

    @Override
    protected void handleAction(ServerClient client, Message message) {
        client.receiveObject(message);
    }
}
