package application.view.gui.panels;

import application.controller.server.AuthorisationManager;
import application.controller.server.exceptions.ServerException;
import application.model.collection.CollectionManager;
import application.model.data.worker.Worker;
import application.view.gui.AbstractFrame;
import application.view.gui.ApplicationEvent;
import application.view.gui.ApplicationMediator;
import application.view.gui.AuthorizationFrame;
import application.view.gui.panels.table.graphics.GraphicsPanel;
import application.view.gui.panels.table.TablePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutionException;

public class ApplicationFrame<T extends Worker> extends AbstractFrame
        implements ApplicationMediator<T>, BlockableComponent {

    public static final Logger logger = LoggerFactory.getLogger(ApplicationFrame.class);

    private final HeaderPanel headerPanel;
    private final LeftSidePanel leftSidePanel;
    private final RightSidePanel<T> rightSidePanel;
    private final FooterPanel footerPanel;
    private final TablePanel<T> tablePanel;
    private final CollectionManager<T> collectionManager;
    private final AuthorisationManager authorizationManager;
    private final GraphicsPanel<T> graphicsPanel;
    private final String username;
    private final JTabbedPane tabbedPane;

    private final Timer timer;

    public ApplicationFrame(CollectionManager<T> collectionManager, AuthorisationManager authorizationManager, String username) {
        this.collectionManager = collectionManager;
        this.authorizationManager = authorizationManager;
        this.username = username;
        headerPanel = new HeaderPanel(this);
        leftSidePanel = new LeftSidePanel(this);
        footerPanel = new FooterPanel(this);
        tablePanel = new TablePanel<>(this);
        rightSidePanel = new RightSidePanel(this);
        graphicsPanel = new GraphicsPanel<>(this);


        tabbedPane = new JTabbedPane();
        addChildToLocalize(leftSidePanel);
        addChildToLocalize(footerPanel);
        addChildToLocalize(tablePanel);
        addChildToLocalize(headerPanel);
        addChildToLocalize(rightSidePanel);
        initGUI();

        timer = new Timer(7000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                triggerEvent(ApplicationEvent.update(false));
            }
        });
        timer.start();
        triggerEvent(ApplicationEvent.update(true));
    }


    public void update(boolean always) {
        timer.stop();

        new SwingWorker<java.util.List<T>, Void>() {
            @Override
            protected java.util.List<T> doInBackground() {
                java.util.List<T> list = null;
                for (int i = 0, k = 0; i < 5; i++)
                    try {
                        list = collectionManager.asList();
                        break;
                    } catch (ServerException e) {
                        e.printStackTrace();
                        if (k++ == 4) triggerEvent(ApplicationEvent.error(e));
                    }
                return list;
            }

            @Override
            protected void done() {
                try {
                    java.util.List<T> list = get();
                    if (list == null) return;
                    tablePanel.refresh(list, always);
                    graphicsPanel.refresh(list);
                    timer.start();
                } catch (InterruptedException | ExecutionException ignored) {
                }
            }
        }.execute();
    }

    public CollectionManager<T> getCollectionManager() {
        return collectionManager;
    }

    @Override
    protected void initGUI() {
        super.initGUI();
        constructLayout();

        setTitle(bundle.getString("user") + ": " + username);
        setMinimumSize(new Dimension(1252, 900));
        setSize(1000, 800);
        setMaximumSize(new Dimension(1920, 1080));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void constructLayout() {
        setLayout(new BorderLayout());


        JPanel tabbedPaneWrapper = new JPanel();


        tabbedPaneWrapper.setLayout(new BorderLayout());
        tabbedPane.addTab(bundle.getString("|"), tablePanel);
        tabbedPane.addTab(bundle.getString("||"), graphicsPanel);
        tabbedPaneWrapper.add(tabbedPane, BorderLayout.CENTER);


        add(headerPanel, BorderLayout.NORTH);
        add(leftSidePanel, BorderLayout.WEST);
        add(tabbedPaneWrapper, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);
        add(rightSidePanel, BorderLayout.EAST);
    }

    @Override
    public void updateLocale() {
        setTitle(bundle.getString("user") + ": " + username);
    }

    private void removeById(Long id) {
        new SwingWorker<Boolean, Void>() {
            @Override
            protected void done() {
                try {
                    get();
                    triggerEvent(ApplicationEvent.update(true));
                } catch (ExecutionException e) {
                    triggerEvent(ApplicationEvent.error(e.getCause()));
                } catch (InterruptedException ignored) {
                } finally {
                }
            }

            @Override
            protected Boolean doInBackground() {
                return collectionManager.removeById(id);
            }
        }.execute();
    }

    @Override
    public void block() {
        headerPanel.block();
        leftSidePanel.block();
        footerPanel.block();
        tablePanel.block();
        rightSidePanel.block();
    }

    @Override
    public void unblock() {
        headerPanel.unblock();
        leftSidePanel.unblock();
        footerPanel.unblock();
        tablePanel.unblock();
        rightSidePanel.unblock();
    }

    public void add() {
        new SwingWorker<T, Void>() {
            @Override
            protected void done() {
                try {
                    rightSidePanel.addMode(get());
                } catch (InterruptedException ignored) {
                } catch (ExecutionException e) {
                    triggerEvent(ApplicationEvent.error(e.getCause()));
                }
            }

            @Override
            protected T doInBackground() throws Exception {
                return collectionManager.generateNew();
            }
        }.execute();
    }

    @Override
    public void triggerEvent(ApplicationEvent event) {
        logger.debug("Triggered {} event!", event.type);
        switch (event.type) {
            case ERROR:
                ApplicationEvent.ErrorEvent errorEvent = (ApplicationEvent.ErrorEvent) event;
                JOptionPane.showMessageDialog(new JFrame(), errorEvent.param+bundle.getString(errorEvent.exception.getClass().getSimpleName()), bundle.getString("error"), JOptionPane.ERROR_MESSAGE);
                if (((ApplicationEvent.ErrorEvent) event).exception.getClass().isAssignableFrom(ServerException.class))
                    triggerEvent(ApplicationEvent.close());
                break;
            case UPDATE:
                ApplicationEvent.UpdateEvent updateEvent = (ApplicationEvent.UpdateEvent) event;
                update(updateEvent.always);
                break;
            case DELETE: {
                Long id = ((ApplicationEvent.DeleteEvent) event).id;
                removeById(id);
                break;
            }
            case CLOSE: {
                timer.stop();
                footerPanel.close();
                collectionManager.close();
                new AuthorizationFrame(authorizationManager, (str) -> {
                    ApplicationFrame<T> applicationFrame = new ApplicationFrame<>(collectionManager, authorizationManager, str);
                    applicationFrame.setVisible(true);
                }).setVisible(true);
                dispose();
                break;
            }
            case INFO: {
                JOptionPane.showMessageDialog(new JFrame(), bundle.getString(((ApplicationEvent.InfoEvent) event).getKey()), "Info", JOptionPane.INFORMATION_MESSAGE);
                break;
            }
            case BLOCK: {
                timer.stop();
                block();
                break;
            }
            case UNBLOCK: {
                unblock();
                triggerEvent(ApplicationEvent.update(true));
                timer.restart();
                break;
            }
            case UPDATEITEM: {
                rightSidePanel.updateItem(collectionManager.getById(((ApplicationEvent.UpdateItemEvent) event).id));
                break;
            }
            case ADD: {
                add();
                break;
            }
        }
    }
}
