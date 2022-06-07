import application.controller.ApplicationController;
import application.controller.actions.LoginAction;
import application.controller.commands.worker.CountByPositionCommand;
import application.controller.commands.worker.FilterGreaterThanPositionCommand;
import application.controller.commands.worker.SumOfSalaryCommand;
import application.controller.server.TCPServer;
import application.model.collection.CollectionManager;
import application.model.collection.StackCollectionManager;
import application.model.collection.server.TCPClientCollectionManager;
import application.model.collection.server.TCPServerCollectionManager;
import application.model.data.worker.Worker;

public class Client {
    public static void main(String... args) {
        TCPClientCollectionManager<Worker> tcpClientCollectionManager = new TCPClientCollectionManager<Worker>("localhost", 8300) {
            @Override
            public Class<Worker> getElemsClass() {
                return Worker.class;
            }
        };

        ApplicationController<Worker> controller = new ApplicationController<>(tcpClientCollectionManager);

        controller.getCommandManager().addCommand(new CountByPositionCommand<>(tcpClientCollectionManager));
        controller.getCommandManager().addCommand(new SumOfSalaryCommand<>(tcpClientCollectionManager));
        controller.getCommandManager().addCommand(new FilterGreaterThanPositionCommand<>(tcpClientCollectionManager));

        controller.getActionManager().add(tcpClientCollectionManager::init);
        controller.getActionManager().add(new LoginAction(controller.getInputManager(), tcpClientCollectionManager));

        controller.run();
    }
}
