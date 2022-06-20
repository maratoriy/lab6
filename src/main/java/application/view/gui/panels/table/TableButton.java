package application.view.gui.panels.table;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.EventObject;
import java.util.Vector;

public class TableButton extends JButton implements TableCellRenderer, TableCellEditor {
    private int selectedRow;
    private int selectedColumn;
    Vector<TableButtonListener> listener;

    public TableButton(String text) {
        super(text);
        this.setMargin(new Insets(5, 5, 5, 5));
        listener = new Vector<>();
        addActionListener(e -> {
            for (TableButtonListener l : listener) {
                l.tableButtonClicked(selectedRow, selectedColumn);
            }
        });
    }

    public void addTableButtonListener(TableButtonListener l) {
        listener.add(l);
    }

    public void removeTableButtonListener(TableButtonListener l) {
        listener.remove(l);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int col) {
        return this;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 int row,
                                                 int col) {
        selectedRow = row;
        selectedColumn = col;
        return this;
    }

    @Override
    public void addCellEditorListener(CellEditorListener arg0) {
    }

    @Override
    public void cancelCellEditing() {
    }

    @Override
    public Object getCellEditorValue() {
        return "";
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        return true;
    }

    @Override
    public void removeCellEditorListener(CellEditorListener arg0) {
    }

    @Override
    public boolean shouldSelectCell(EventObject arg0) {
        return true;
    }

    @Override
    public boolean stopCellEditing() {
        return true;
    }
}
