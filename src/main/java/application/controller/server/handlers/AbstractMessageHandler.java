package application.controller.server.handlers;

import application.controller.server.client.ServerClient;
import application.controller.server.messages.ClientMessage;

abstract public class AbstractMessageHandler implements MessageHandler {
    protected AbstractMessageHandler next;

    public AbstractMessageHandler() {
    }


    public AbstractMessageHandler addNext(AbstractMessageHandler next) {
        if (this.next != null)
            this.next.addNext(next);
        else this.next = next;
        return this;
    }


    abstract protected void handleAction(ServerClient client, ClientMessage message);

    final public static class NewStreamPipe extends AbstractMessageHandler {

        @Override
        public void handleObject(ServerClient client, ClientMessage message) {
            if (next != null) {
                handleAction(client, message);
            }
        }

        @Override
        protected void handleAction(ServerClient client, ClientMessage message) {
            Runnable threadRun = () -> {
                next.handleObject(client, message);
            };
            Thread newThread = new Thread(threadRun);
            newThread.setDaemon(true);
            newThread.start();
        }

    }

}
