package application.controller.server.handlers;

import application.controller.server.client.ClientTask;
import application.controller.server.client.ServerClient;

public class ClientTaskHandler extends AbstractObjectHandler<Object> {

    @Override
    public void action(ServerClient client, Object object) {
        if(client.haveTask()&&client.getTopTaskType()==ClientTask.Type.READ) {
            ClientTask.ReadTask<?> readTask = (ClientTask.ReadTask<?>) client.pollTask();
            readTask.accept(object);
        }
    }

    public ClientTaskHandler() {
        super(Object.class);
    }
}
