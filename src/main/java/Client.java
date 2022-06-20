import application.controller.ConsoleApplicationController;
import application.controller.actions.LoginAction;
import application.controller.commands.worker.CountByPositionCommand;
import application.controller.commands.worker.FilterGreaterThanPositionCommand;
import application.controller.commands.worker.SumOfSalaryCommand;
import application.model.collection.server.TCPClientCollectionManager;
import application.model.data.worker.Worker;

public class Client {
    public static void main(String... args) {
        TCPClientCollectionManager<Worker> tcpClientCollectionManager = new TCPClientCollectionManager<Worker>("localhost", 8080) {
            @Override
            public Class<Worker> getElemsClass() {
                return Worker.class;
            }
        };

        ConsoleApplicationController<Worker> controller = new ConsoleApplicationController<>(tcpClientCollectionManager);

        controller.getCommandManager().addCommand(new CountByPositionCommand<>(tcpClientCollectionManager));
        controller.getCommandManager().addCommand(new SumOfSalaryCommand<>(tcpClientCollectionManager));
        controller.getCommandManager().addCommand(new FilterGreaterThanPositionCommand<>(tcpClientCollectionManager));

        controller.getActionManager().add(tcpClientCollectionManager::init);
        controller.getActionManager().add(new LoginAction(controller.getInputManager(), tcpClientCollectionManager));

        controller.run();

    }
}
