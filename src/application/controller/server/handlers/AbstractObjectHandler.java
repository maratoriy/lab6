package application.controller.server.handlers;

import application.controller.server.TCPServer;
import application.controller.server.client.ServerClient;

abstract public class AbstractObjectHandler<T> implements ObjectHandler<Object> {
    protected final Class<T> tClass;
    protected AbstractObjectHandler<?> next;

    public AbstractObjectHandler(Class<T> tClass) {
        this.tClass = tClass;
    }

    abstract public void action(ServerClient client, T object);

    public AbstractObjectHandler<T> addNext(AbstractObjectHandler<?> handler) {
        if(next!=null) {
            next.addNext(handler);
        } else {
            this.next = handler;
        }
        return this;
    }

    @Override
    public void handleObject(ServerClient client, Object object) {
        if (tClass.isAssignableFrom(object.getClass())) {
            action(client, tClass.cast(object));
        } else if (next != null) {
            next.handleObject(client, object);
        }
    }
}
