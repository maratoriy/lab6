package application.view.gui.panels;

import application.model.collection.CollectionItem;
import application.model.data.worker.Worker;
import application.view.gui.AbstractPanel;
import application.view.gui.ApplicationEvent;
import application.view.gui.ApplicationMediator;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.concurrent.ExecutionException;

public class LeftSidePanel<T extends Worker> extends AbstractPanel<T> {

    private final JButton clearButton = new JButton(bundle.getString("clearButton"));
    private final JButton updateButton = new JButton(bundle.getString("updateButton"));
    private final JButton addButton = new JButton(bundle.getString("addButton"));

    @Override
    public void updateLocale() {
        clearButton.setText(bundle.getString("clearButton"));
        updateButton.setText(bundle.getString("updateButton"));
        addButton.setText(bundle.getString("addButton"));
        ((TitledBorder) getBorder()).setTitle(bundle.getString("commandPanel"));
    }

    public LeftSidePanel(ApplicationMediator<T> applicationMediator) {
        super(applicationMediator);
        initGUI();
    }


    protected void constructLayouts() {
        setLayout(new GridLayout(15, 1, 0, 2));


        add(clearButton);
        add(updateButton);
        add(addButton);

        Border border = BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY);
        setBorder(BorderFactory.createTitledBorder(border, bundle.getString("commandPanel")));
    }

    @Override
    public void block() {
        updateButton.setEnabled(false);
        clearButton.setEnabled(false);
        addButton.setEnabled(false);
    }

    @Override
    public void unblock() {
        updateButton.setEnabled(true);
        clearButton.setEnabled(true);
        addButton.setEnabled(true);
    }

    @Override
    protected void addListeners() {
        clearButton.addActionListener(event -> {
            new SwingWorker<Void, Void>() {
                @Override
                protected void done() {
                    try {
                        get();
                    } catch (ExecutionException e) {
                        applicationMediator.triggerEvent(ApplicationEvent.error(e.getCause()));
                    } catch (InterruptedException ignored) {
                    }
                }

                @Override
                protected Void doInBackground() {
                    applicationMediator.getCollectionManager().clear();
                    applicationMediator.triggerEvent(ApplicationEvent.update(true));
                    return null;
                }
            }.execute();
        });
        updateButton.addActionListener(event -> {
            applicationMediator.triggerEvent(ApplicationEvent.update(true));
        });
        addButton.addActionListener(e -> {
            applicationMediator.triggerEvent(ApplicationEvent.add());
        });
    }
}
