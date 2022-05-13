package application.controller.server.handlers;

import application.controller.server.client.ServerClient;

@FunctionalInterface
public interface ObjectHandler<T> {
    void handleObject(ServerClient client, T object);
}
