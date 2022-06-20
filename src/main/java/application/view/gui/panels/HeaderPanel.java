package application.view.gui.panels;

import application.model.collection.CollectionItem;
import application.model.data.worker.Worker;
import application.view.gui.AbstractPanel;
import application.view.gui.ApplicationEvent;
import application.view.gui.ApplicationMediator;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class HeaderPanel<T extends Worker> extends AbstractPanel<T> {
    private final JButton exitButton = new JButton(bundle.getString("exitButton"));

    public HeaderPanel(ApplicationMediator<T> applicationMediator) {
        super(applicationMediator);
        initGUI();
    }

    @Override
    protected void constructLayouts() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        add(Box.createHorizontalGlue());
        add(exitButton, RIGHT_ALIGNMENT);

        Border border = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY);
        setBorder(border);
    }

    @Override
    public void block() {
//        exitButton.setEnabled(false);
    }

    @Override
    public void unblock() {
//        exitButton.setEnabled(true);
    }

    @Override
    protected void addListeners() {
        exitButton.addActionListener(event -> {
            applicationMediator.triggerEvent(ApplicationEvent.close());
        });
    }

    @Override
    public void updateLocale() {
        exitButton.setText(bundle.getString("exitButton"));
    }
}
