package application.controller.server.client;

import java.util.function.Consumer;

abstract public class ClientTask {
    private final Type type;

    public ClientTask(Type type) {
        this.type = type;
    }

    static public <T> ReadTask<T> readTask(Class<T> tClass, Consumer<T> tConsumer) {
        return new ReadTask<>(tClass, tConsumer);
    }

    static public WriteTask writeTask(Object object) {
        return new WriteTask(object);
    }

    public enum Type {
        READ,
        WRITE
    }

    public Type getType() {
        return type;
    }

    static public final class ReadTask<T> extends ClientTask implements Consumer<Object> {
        private final Class<T> tClass;
        private final Consumer<T> tConsumer;

        public ReadTask(Class<T> tClass, Consumer<T> tConsumer) {
            super(Type.READ);
            this.tClass = tClass;
            this.tConsumer = tConsumer;
        }

        @Override
        public void accept(Object o) {
            if(tClass.isAssignableFrom(o.getClass()))
                tConsumer.accept(tClass.cast(o));
        }
    }

    static public final class WriteTask extends ClientTask {
        private final Object object;

        public WriteTask(Object object) {
            super(Type.WRITE);
            this.object = object;
        }

        public Object getObject() {
            return object;
        }
    }
}
