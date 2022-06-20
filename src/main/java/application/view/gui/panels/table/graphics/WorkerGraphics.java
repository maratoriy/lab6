package application.view.gui.panels.table.graphics;

import application.model.data.worker.Worker;

import javax.swing.*;
import java.awt.*;
import java.nio.charset.StandardCharsets;

public class WorkerGraphics<T extends Worker> extends JButton {
    private final T worker;
    private final Dimension panelSize;
    private int alpha = 45;
    private int beta = 45;
    private final Color color;
    private final double k = 3.5;

    public WorkerGraphics(T worker, Dimension panelSize) {
        this.worker = worker;
        this.panelSize = panelSize;
        this.color = getColorByName(worker.getUser());

        setGraphicsBounds(panelSize);

    }

    private Color getColorByName(String username) {
        byte[] bytes = username.getBytes(StandardCharsets.UTF_8);
        int myRGB = 0;
        for (byte b: bytes) myRGB += b;
        return new Color(myRGB * myRGB * myRGB);
    }

    private void setGraphicsBounds(Dimension graphicsPanelSize) {
        int x = (int) Math.floor(Math.min(worker.getCoordinates().getX(), graphicsPanelSize.getWidth() - scale(36)));
        int y = (int) Math.floor(Math.min(worker.getCoordinates().getY(), graphicsPanelSize.getHeight() - scale(52)));
        setBounds(x, y, scale(36), scale(52));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;

        Color backgroundColor = UIManager.getColor ("Panel.background");
        g.setColor(backgroundColor);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(color);

        g.fillRect(scale(17), scale(20), scale(6), scale(10));


        g.fillRect(scale(8), scale(35), scale(36-10), scale(6));


        g.fillOval(scale(2), scale(1), scale(18), scale(18));
        g.fillOval(scale(16), scale(1), scale(18), scale(18));

        g.setColor(backgroundColor);
        for(int i=0;i<9;i++)
            fillRotatingCircle(g, scale(2), scale(1), scale(18), alpha+i*40-10);

        for(int i=0;i<9;i++)
            fillRotatingCircle(g, scale(16), scale(1), scale(18), beta+i*40);
    }

    private void fillRotatingCircle(Graphics2D g, int bigCircleX, int bigCircleY, int bigCircleDiameter, int alphaDegree) {
        int r = 5;
        double x = bigCircleX + (double) bigCircleDiameter/2 + k * r * Math.cos(Math.toRadians(alphaDegree));
        double y = bigCircleY + (double) bigCircleDiameter/2 + k * r * Math.sin(Math.toRadians(alphaDegree));
        g.fill(new RotatingCircle(x, y, scale(5)));
    }

    public void transitionStep1() {
        alpha += 2;
        if (alpha >= 360) alpha -= 360;
    }

    public void transitionStep2() {
        beta -= 2;
        if (beta == 0) beta += 360;
    }

    private int scale(int size) {
        return (int) Math.round(size * k);
    }

}
