import application.controller.ApplicationController;
import application.controller.actions.OpenFileAction;
import application.controller.commands.basic.SaveCommand;
import application.controller.commands.worker.CountByPositionCommand;
import application.controller.commands.worker.FilterGreaterThanPositionCommand;
import application.controller.commands.worker.SumOfSalaryCommand;
import application.model.collection.StackCollectionManager;
import application.model.collection.server.TCPClientCollectionManager;
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
                return new Worker(generateId());
            }
        };

        TCPServerCollectionManager<Worker> serverCollectionManager = new TCPServerCollectionManager<>(workerCollectionManager, "localhost", 8300);

        ApplicationController<Worker> controller = new ApplicationController<Worker>(serverCollectionManager) {
            @Override
            public void close() {
                getCommandManager().execCommand("save", null);
                super.close();
            }
        };


        if(args.length>0) {
            controller.setSavePath(String.join(" ", args));
        } else {
            controller.setSavePath("default.xml");
        }

        controller.getCommandManager().addCommand(new CountByPositionCommand<>(workerCollectionManager));
        controller.getCommandManager().addCommand(new SumOfSalaryCommand<>(workerCollectionManager));
        controller.getCommandManager().addCommand(new FilterGreaterThanPositionCommand<>(workerCollectionManager));
        controller.getCommandManager().addCommand(new SaveCommand(controller, workerCollectionManager));


        controller.getActionManager().add(new OpenFileAction(controller, controller.getSavePath(), workerCollectionManager));

        controller.run();
    }
}
