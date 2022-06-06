package application.controller.server.handlers;

import application.controller.server.Message;
import application.controller.server.client.ServerClient;

@FunctionalInterface
public interface MessageHandler {
    void handleObject(ServerClient client, Message message);
}
