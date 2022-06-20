package application.view.gui.panels.table;

import application.model.collection.CollectionItem;
import application.model.data.worker.Worker;
import application.view.datamodels.StringTable;
import application.view.gui.AbstractPanel;
import application.view.gui.ApplicationMediator;
import application.view.gui.Layouts;

import javax.swing.*;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;

public class InfoPanel<T extends Worker> extends AbstractPanel<T> {
    private final JLabel initializationTimeLabel = new JLabel(bundle.getString("initializationTimeLabel")+": ");
    private final JLabel sizeLabel = new JLabel(bundle.getString("sizeLabel")+": ");
    private int size;
    private LocalDateTime initializationTime;

    public InfoPanel(ApplicationMediator<T> applicationMediator) {
        super(applicationMediator);
        initGUI();
        refresh();
    }

    @Override
    protected void constructLayouts() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        add(Layouts.hspace(20));
        add(sizeLabel, LEFT_ALIGNMENT);
        add(Box.createHorizontalGlue());
        add(initializationTimeLabel, RIGHT_ALIGNMENT);
    }

    @Override
    protected void addListeners() {}

    @Override
    public void updateLocale() {
        initializationTimeLabel.setText(bundle.getString("initializationTimeLabel")+": "+initializationTime.toString());
        sizeLabel.setText(bundle.getString("sizeLabel")+": "+size);
    }

    public void refresh() {
        new SwingWorker<StringTable, Void>() {
            @Override
            protected StringTable doInBackground() throws Exception {
                return applicationMediator.getCollectionManager().getCollectionInfo();
            }

            @Override
            protected void done() {
                try {
                    StringTable result = get();
                    initializationTime = LocalDateTime.parse(result.getTable().get(0).get("initializationTime"));
                    size = Integer.parseInt(result.getTable().get(0).get("size"));
                    updateLocale();
                } catch (InterruptedException | ExecutionException ignored) {
                }
            }
        }.execute();
    }

    @Override
    public void block() {}

    @Override
    public void unblock() {}
}
