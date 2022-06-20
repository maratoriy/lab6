package application.controller.server.handlers;

import application.controller.server.TCPServer;
import application.controller.server.client.ServerClient;
import application.controller.server.messages.ClientMessage;
import application.controller.server.messages.Message;

public class ClearMessageHandler extends PipeTypeAction {
    public ClearMessageHandler() {
        super(Message.Type.CLEAR);
    }

    @Override
    protected void handleAction(ServerClient client, ClientMessage message) {
        TCPServer.log("Cleared all task on client {}", client.getName());
        client.clear();
    }
}
