package application.view.gui;

public class ApplicationEvent {
    public final Type type;

    protected ApplicationEvent(Type type) {
        this.type = type;
    }

    public static ErrorEvent error(Throwable e) {
        return new ErrorEvent("",e);
    }

    public static ErrorEvent error(String param, Throwable e) {
        return new ErrorEvent(param, e);
    }

    public static UpdateEvent update(boolean always) {
        return new UpdateEvent(always);
    }

    public static DeleteEvent delete(Long id) {
        return new DeleteEvent(id);
    }

    public static UpdateItemEvent updateItemEvent(Long id) {
        return new UpdateItemEvent(id);
    }

    public static ApplicationEvent close() {
        return new ApplicationEvent(Type.CLOSE);
    }

    public static ApplicationEvent info(String key) {
        return new InfoEvent(key);
    }

    public static ApplicationEvent block() { return new ApplicationEvent(Type.BLOCK); }

    public static ApplicationEvent unblock() { return new ApplicationEvent(Type.UNBLOCK); }

    public static ApplicationEvent add() {
        return new ApplicationEvent(Type.ADD);
    }

    final static public class InfoEvent extends ApplicationEvent {
        private final String key;

        public InfoEvent(String key) {
            super(Type.INFO);
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    final static public class UpdateEvent extends ApplicationEvent {
        public final boolean always;
        public UpdateEvent(boolean always) {
            super(Type.UPDATE);
            this.always = always;
        }
    }

    final static public class ErrorEvent extends ApplicationEvent {
        public final Throwable exception;
        public String param="";
        public ErrorEvent(String param, Throwable e) {
            super(Type.ERROR);
            this.param = param;
            this.exception = e;
        }
    }

    final static public class DeleteEvent extends ApplicationEvent {
        public final Long id;

        public DeleteEvent(Long id) {
            super(Type.DELETE);
            this.id = id;
        }
    }


    final static public class UpdateItemEvent extends ApplicationEvent {
        public final Long id;

        public UpdateItemEvent(Long id) {
            super(Type.UPDATEITEM);
            this.id = id;
        }
    }


    public enum Type {
        ERROR,
        ADD,
        UPDATE,
        INFO,
        DELETE,
        UPDATEITEM,
        CLOSE,
        BLOCK,
        UNBLOCK
    }
}
