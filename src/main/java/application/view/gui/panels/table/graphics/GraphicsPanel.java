package application.view.gui.panels.table.graphics;

import application.model.data.worker.Worker;
import application.view.gui.AbstractPanel;
import application.view.gui.ApplicationEvent;
import application.view.gui.ApplicationMediator;
import application.view.gui.panels.table.TablePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class GraphicsPanel<T extends Worker> extends AbstractPanel<T> {

    private java.util.List<Timer> timers;

    public GraphicsPanel(ApplicationMediator<T> applicationMediator) {
        super(applicationMediator);
        initGUI();
    }

    @Override
    protected void constructLayouts() {
        setLayout(null);
    }

    private java.util.List<T> currentList;
    public void refresh(java.util.List<T> list) {
        if(currentList==null||currentList.size()!=list.size()||TablePanel.compare(currentList, list)) {
            currentList = list;
            Dimension graphicsPanelSize = new Dimension(getVisibleRect().width, getVisibleRect().height);

            if (timers != null) {
                for (Timer timer : timers)
                    timer.stop();
                timers.clear();
            }

            removeAll();
            repaint();
            revalidate();


            timers = new ArrayList<>(list.size());
            list.forEach(
                    worker -> {
                        WorkerGraphics<T> mg = new WorkerGraphics<>(worker, graphicsPanelSize);
                        add(mg);

                        mg.addActionListener(event -> {
                            applicationMediator.triggerEvent(ApplicationEvent.updateItemEvent(worker.getId()));
                        });

                        Timer timer = new Timer(40, new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                mg.transitionStep1();
                                mg.transitionStep2();
                                repaint();
                                revalidate();
                            }
                        });
                        timers.add(timer);
                        timer.start();
                    }
            );
        }
    }

    @Override
    protected void addListeners() {

    }

    @Override
    public void updateLocale() {

    }

    @Override
    public void block() {

    }

    @Override
    public void unblock() {

    }
}
