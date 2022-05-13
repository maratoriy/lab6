package application.controller.server.handlers;

import application.controller.server.client.ServerClient;
import application.controller.server.exceptions.ServerException;

public class ExceptionHandler extends AbstractObjectHandler<RuntimeException> {

    public ExceptionHandler() {
        super(RuntimeException.class);
    }

    @Override
    public void action(ServerClient client, RuntimeException object) {
        client.clearTaskSet();
        throw new ServerException(object);
    }
}
