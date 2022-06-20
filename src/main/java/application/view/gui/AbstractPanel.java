package application.view.gui;

import application.model.collection.CollectionItem;
import application.model.data.worker.Worker;
import application.view.gui.localization.BundleManager;
import application.view.gui.localization.LocalizableComponent;
import application.view.gui.panels.BlockableComponent;

import javax.swing.*;

abstract public class AbstractPanel<T extends Worker> extends JPanel
        implements LocalizableComponent, BlockableComponent {
    protected final BundleManager bundle = BundleManager.getBundle("gui");
    protected final ApplicationMediator<T> applicationMediator;

    public AbstractPanel(ApplicationMediator<T> applicationMediator) {
        this.applicationMediator = applicationMediator;
    }

    protected void initGUI() {
        constructLayouts();
        addListeners();
    }

    public void close() {}

    abstract protected void constructLayouts();

    abstract protected void addListeners();
}
