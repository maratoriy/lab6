import application.controller.GUIWorkerApplicationController;
import application.model.collection.server.TCPClientCollectionManager;
import application.model.data.worker.Worker;
import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

public class ClientGUI {
    public static void main(String... args) {
        FlatLightLaf.install();
        try {
            UIManager.setLookAndFeel( new FlatDarculaLaf() );
        } catch( Exception ex ) {
            System.err.println( "Failed to initialize LaF" );
        }


        TCPClientCollectionManager<Worker> tcpClientCollectionManager = new TCPClientCollectionManager<Worker>("localhost", 8080) {
            @Override
            public Class<Worker> getElemsClass() {
                return Worker.class;
            }
        };
        Runtime.getRuntime().addShutdownHook(new Thread(tcpClientCollectionManager::close));
//        tcpClientCollectionManager.init();

        GUIWorkerApplicationController<Worker> guiWorkerApplicationController = new GUIWorkerApplicationController<>(tcpClientCollectionManager);
        guiWorkerApplicationController.setAuthorizationManager(tcpClientCollectionManager);

        guiWorkerApplicationController.run();
    }
}
