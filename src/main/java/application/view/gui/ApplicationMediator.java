package application.view.gui;

import application.model.collection.CollectionItem;
import application.model.collection.CollectionManager;
import application.model.data.worker.Worker;

public interface ApplicationMediator<T extends Worker> {
    void triggerEvent(ApplicationEvent event);

    CollectionManager<T> getCollectionManager();
}
