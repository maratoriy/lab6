import application.controller.ConsoleApplicationController;
import application.controller.commands.worker.CountByPositionCommand;
import application.controller.commands.worker.FilterGreaterThanPositionCommand;
import application.controller.commands.worker.SumOfSalaryCommand;
import application.model.collection.StackCollectionManager;
import application.model.collection.database.DatabaseCollectionManager;
import application.model.collection.server.TCPServerCollectionManager;
import application.model.data.worker.Worker;

public class Server {
    public static void main(String... args) {
        StackCollectionManager<Worker> workerCollectionManager = new StackCollectionManager<Worker>() {
            @Override
            public Class<Worker> getElemsClass() {
                return Worker.class;
            }

            @Override
            public Worker generateNew() {
                return new Worker(1L);
            }
        };

        DatabaseCollectionManager<Worker> databaseCollectionManager = new DatabaseCollectionManager<>(workerCollectionManager);
        databaseCollectionManager.dataBaseName = "workers";
        databaseCollectionManager.parse();

        TCPServerCollectionManager<Worker> serverCollectionManager = new TCPServerCollectionManager<>(databaseCollectionManager, "localhost", 8080);

        ConsoleApplicationController<Worker> controller = new ConsoleApplicationController<Worker>(serverCollectionManager);


        controller.getCommandManager().addCommand(new CountByPositionCommand<>(workerCollectionManager));
        controller.getCommandManager().addCommand(new SumOfSalaryCommand<>(workerCollectionManager));
        controller.getCommandManager().addCommand(new FilterGreaterThanPositionCommand<>(workerCollectionManager));
//        controller.getCommandManager().addCommand(new SaveCommand(controller, workerCollectionManager));


//        controller.getActionManager().add(new OpenFileAction(controller, controller.getSavePath(), workerCollectionManager));

        controller.run();
    }
}
