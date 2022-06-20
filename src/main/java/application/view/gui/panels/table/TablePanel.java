package application.view.gui.panels.table;

import application.controller.server.TCPServer;
import application.model.data.worker.Organization;
import application.model.data.worker.Worker;
import application.view.gui.AbstractPanel;
import application.view.gui.ApplicationEvent;
import application.view.gui.ApplicationMediator;
import application.view.gui.panels.ApplicationFrame;
import application.view.gui.panels.table.addition.TableColumnAdjuster;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class TablePanel<T extends Worker> extends AbstractPanel<T> {
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JScrollPane jScrollPane = new JScrollPane();
    private final TableButton buttonEditor = new TableButton(bundle.getString("deleteButton"));
    private final InfoPanel infoPanel;
    private final TableRowSorter<TableModel> sorter;
    private final FilterPanel<T> filterPanel;
    private final List<String> bannedToEditColumns = new ArrayList<>();
    private final TableColumnAdjuster adjuster;

    public TablePanel(ApplicationMediator<T> applicationMediator) {
        super(applicationMediator);
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return !bannedToEditColumns.contains(getColumnName(column));
            }
        };

        infoPanel = new InfoPanel(applicationMediator);
        table = new JTable(tableModel);
        Worker example = new Worker(1L);
        example.setOrganization(new Organization());
        List<String> headers = example.getGettersNames().stream().map(iter -> iter.replace("Worker.", "")).collect(Collectors.toList());
        bannedToEditColumns.addAll(headers);
        tableModel.setColumnIdentifiers(headers.toArray(new String[0]));


        table.setModel(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getTableHeader().setResizingAllowed(false);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(18);
        table.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int columnIndex = 0; columnIndex < tableModel.getColumnCount(); columnIndex++) {
            table.getColumnModel().getColumn(columnIndex).setCellRenderer(renderer);
        }
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sorter = new TableRowSorter<>(table.getModel());
        addDeleteButton();

        adjuster = new TableColumnAdjuster(table);
        filterPanel = new FilterPanel<>(applicationMediator, this);

        initGUI();
    }

    @Override
    public void block() {
        table.setEnabled(false);
        infoPanel.block();
        filterPanel.block();
    }

    @Override
    public void unblock() {
        table.setEnabled(true);
        infoPanel.unblock();
        filterPanel.unblock();
    }

    public void addDeleteButton() {
        tableModel.addColumn("Action");
        table.moveColumn(table.getColumnCount() - 1, 0);

        TableColumn col = table.getColumn("Action");
        col.setCellRenderer(buttonEditor);
        col.setCellEditor(buttonEditor);
    }

    public void resize() {
        adjuster.adjustColumns();
        filterPanel.updateSize();
    }



    public JTable getTable() {
        return table;
    }

    public DefaultTableModel getTableModel() {
        return tableModel;
    }

    @Override
    protected void constructLayouts() {
        addSorter();
        resize();

        setLayout(new BorderLayout());

        JScrollPane filterPane = new JScrollPane(filterPanel);
        filterPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane.setViewportView(table);



        jScrollPane.getHorizontalScrollBar().addAdjustmentListener(e -> {
                filterPane.getHorizontalScrollBar().setValue(e.getValue());
        });

        add(filterPane, BorderLayout.NORTH);
        add(jScrollPane, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.SOUTH);
    }

    @Override
    protected void addListeners() {
        buttonEditor.addTableButtonListener((row, col) -> {
            Long id = (Long) table.getValueAt(row, 1);
            applicationMediator.triggerEvent(ApplicationEvent.delete(id));
        });
        table.getSelectionModel().addListSelectionListener(event -> {
            if(table.getColumnName(table.getSelectedColumn()).contains("Action")) return;
            try {
                Long id = (Long) table.getModel().getValueAt(table.getSelectedRow(), 0);
                applicationMediator.triggerEvent(ApplicationEvent.updateItemEvent(id));
            } catch (ArrayIndexOutOfBoundsException ignored) {}
        });
    }

    private void addSorter() {
        Comparator<String> stringComparator = Comparator.comparing(value -> value);
        Comparator<Integer> integerComparator = Comparator.comparingInt(value -> value);
        Comparator<Long> longComparator = Comparator.comparingLong(value -> value);
        Comparator<Float> doubleComparator = Comparator.comparingDouble(value -> (double) value);
        Comparator<? extends Enum<?>> enumComparator = Comparator.comparing(Enum::name);
        Comparator<LocalDate> localDateComparator = Comparator.comparing(date -> date,
                (date1, date2) -> {
                    if (date1.isBefore(date2)) {
                        return -1;
                    } else if (date1.isEqual(date2)) {
                        return 0;
                    } else {
                        return 1;
                    }
                });
        Comparator<LocalDateTime> localDateTimeComparator = Comparator.comparing(date -> date,
                (date1, date2) -> {
                    if (date1.isBefore(date2)) {
                        return -1;
                    } else if (date1.isEqual(date2)) {
                        return 0;
                    } else {
                        return 1;
                    }
                });

        List<Comparator<?>> comparators = Arrays.asList(
                longComparator,
                stringComparator,
                stringComparator,
                longComparator,
                integerComparator,
                localDateTimeComparator,
                doubleComparator,
                localDateComparator,
                enumComparator,
                enumComparator,
                stringComparator,
                integerComparator,
                longComparator,
                enumComparator);

        for (int i = 0; i < comparators.size(); i++) {
            sorter.setComparator(i, comparators.get(i));

            table.setRowSorter(sorter);


            sorter.addRowSorterListener(event -> clearSelection());
        }
    }


    public void refreshFilter() {
        clearSelection();
        tableModel.setRowCount(0);
        filterPanel.filter(currentList.stream()).forEach(TablePanel.this::addRow);
    }

    private volatile List<T> currentList = new ArrayList<>();

    synchronized public void refresh(List<T> list, boolean always) {
        new SwingWorker<Boolean, Void>() {
            @Override
            protected void done() {
                try {
                    if(get()) {
                        currentList = list;
                        refreshFilter();
                        resize();
                        infoPanel.refresh();
                    }
                } catch (InterruptedException | ExecutionException e) {
                }
            }

            @Override
            protected Boolean doInBackground() {
                return (currentList.size()!=list.size())||compare(currentList, list)||always;
            }
        }.execute();
    }

    public static <T> boolean compare(List<T> currentList, List<T> list) {
        long count1 = list.stream().filter(currentList::contains).count();
        return count1!=list.size();
    }


    private void addRow(T item) {
        LinkedList<Object> objects = new LinkedList<>(Arrays.asList(
                item.getId(),
                item.getUser(),
                item.getName(),
                item.getCoordinates().getX(),
                item.getCoordinates().getY(),
                item.getCreationDate(),
                item.getSalary(),
                item.getStartDate(),
                item.getPosition(),
                item.getStatus()
        ));
        if (item.getOrganization() != null) {
            Organization o = item.getOrganization();
            objects.addAll(Arrays.asList(
                    o.getFullName(),
                    o.getAnnualTurnover(),
                    o.getEmployeesCount(),
                    o.getType()
            ));
        }
        tableModel.addRow(objects.toArray(new Object[0]));
    }



    @Override
    public void updateLocale() {
        infoPanel.updateLocale();
        filterPanel.updateLocale();
        buttonEditor.setText(bundle.getString("deleteButton"));
    }

    public void clearSelection() {
        table.clearSelection();
    }

}
