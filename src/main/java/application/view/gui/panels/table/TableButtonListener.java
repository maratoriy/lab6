package application.view.gui.panels.table;

import java.util.EventListener;

public interface TableButtonListener extends EventListener {
    void tableButtonClicked(int row, int col);
}
