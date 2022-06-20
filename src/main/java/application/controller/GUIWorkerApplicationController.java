package application.controller;

import application.controller.server.AuthorisationManager;
import application.model.collection.CollectionManager;
import application.model.data.worker.Worker;
import application.view.gui.AuthorizationFrame;
import application.view.gui.panels.ApplicationFrame;

public class GUIWorkerApplicationController<T extends Worker> {
    private final CollectionManager<T> collectionManager;
    private AuthorisationManager authorizationManager;

    public GUIWorkerApplicationController(CollectionManager<T> collectionManager) {
        this.collectionManager = collectionManager;
    }

    public void setAuthorizationManager(AuthorisationManager authorizationManager) {
        this.authorizationManager = authorizationManager;
    }

    public void run() {
        if (authorizationManager != null) {
            new AuthorizationFrame(authorizationManager, (str) -> {
                ApplicationFrame<T> applicationFrame = new ApplicationFrame<>(collectionManager, authorizationManager, str);
                applicationFrame.setVisible(true);
            });
        }
    }


}
