package application.controller.server;

import application.controller.server.client.ServerClient;

abstract public class ClientObserver {
    private final Type type;

    private ClientObserver(Type type) {
        this.type = type;
    }

    public void observe(Type type, ServerClient client) {
        if (this.type == type)
            observeAction(client);
    }

    abstract protected void observeAction(ServerClient client);

    public enum Type {
        WRITE
    }

    static abstract public class WriteObserver extends ClientObserver {
        public WriteObserver() {
            super(Type.WRITE);
        }
    }

}
