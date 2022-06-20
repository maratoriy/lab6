package application.controller.server.handlers;

import application.controller.server.client.ServerClient;
import application.controller.server.messages.ClientMessage;

@FunctionalInterface
public interface MessageHandler {
    void handleObject(ServerClient client, ClientMessage message);
}
