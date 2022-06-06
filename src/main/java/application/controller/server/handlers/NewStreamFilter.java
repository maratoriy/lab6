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
        TCPServer.log("Handler {} will start at new Thread", wrapped.getClass().getSimpleName());
        Runnable threadRun = () -> wrapped.handleObject(client, message);
        Thread handleThread = new Thread(threadRun);
        handleThread.setDaemon(true);
        handleThread.start();
    }
}
