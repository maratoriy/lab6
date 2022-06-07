package application.controller.server.handlers;

import application.controller.server.Message;
import application.controller.server.TCPServer;
import application.controller.server.client.ServerClient;

public class NewStreamFilter extends AbstractMessageHandler {
    private final AbstractMessageHandler wrapped;

    public NewStreamFilter(AbstractMessageHandler wrapped) {
        this.wrapped = wrapped;
        setType(wrapped.getType());
    }


    @Override
    protected void handleAction(ServerClient client, Message message) {
        Runnable threadRun = () -> {
            TCPServer.log("Handler {} started at new Thread {}", wrapped.getClass().getSimpleName(), Thread.currentThread().getName());
            wrapped.handleObject(client, message);
            TCPServer.log("Stopped handler {} at new Thread {}", wrapped.getClass().getSimpleName(), Thread.currentThread().getName());
        };
        Thread handleThread = new Thread(threadRun);
        handleThread.setDaemon(true);
        handleThread.start();
    }
}
