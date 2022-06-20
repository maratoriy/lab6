package application.view.gui.panels.table;

import application.model.data.worker.OrganizationType;
import application.model.data.worker.Position;
import application.model.data.worker.Status;
import application.model.data.worker.Worker;
import application.view.gui.AbstractPanel;
import application.view.gui.ApplicationEvent;
import application.view.gui.ApplicationMediator;
import application.view.gui.Layouts;
import application.view.gui.panels.ApplicationFrame;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Stream;

import static application.view.gui.Layouts.createComboEnum;

public class FilterPanel<T extends Worker> extends AbstractPanel<T> {
    private final TablePanel<T> tablePanel;
    protected final JButton clearFilter = new JButton(bundle.getString("clearFilter"));

    private final JTextField idField = new JTextField();
    private final JTextField userField = new JTextField();
    private final JTextField nameField = new JTextField();
    private final JTextField xField = new JTextField();
    private final JTextField yField = new JTextField();
    private final JTextField creationDateField = new JTextField();
    private final JTextField salaryField = new JTextField();
    private final JTextField startDateField = new JTextField();
    private final JComboBox<String> positionField = createComboEnum(Position.values());
    private final JComboBox<String> statusField = createComboEnum(Status.values());
    private final JTextField fullNameField = new JTextField();
    private final JTextField annualTurnoverField = new JTextField();
    private final JTextField employeesCountField = new JTextField();
    private final JComboBox<String> typeField = createComboEnum(OrganizationType.values());

    public FilterPanel(ApplicationMediator<T> applicationMediator, TablePanel<T> tablePanel) {
        super(applicationMediator);
        this.tablePanel = tablePanel;

        initGUI();
    }



    private boolean check(Object value1, Object value2) {
        if(checkEmpty(value2)) return true;
        return value1.toString().startsWith(value2.toString());
    }

    private boolean checkEmpty(Object value2) {
        return (value2==null||value2.toString().equals(""));
    }

    protected Stream<T> filter(Stream<T> stream) {
        return stream
                .filter(item -> check(item.getId(), idField.getText()))
                .filter(item -> check(item.getUser(), userField.getText()))
                .filter(item -> check(item.getName(), nameField.getText()))
                .filter(item -> check(item.getCoordinates().getX(), xField.getText()))
                .filter(item -> check(item.getCoordinates().getY(), yField.getText()))
                .filter(item -> check(item.getCreationDate(), creationDateField.getText()))
                .filter(item -> check(item.getSalary(), salaryField.getText()))
                .filter(item -> check(item.getStartDate(), startDateField.getText()))
                .filter(item -> check(item.getPosition(), positionField.getSelectedItem()))
                .filter(item -> check(item.getStatus(), statusField.getSelectedItem()))
                .filter(item->  checkEmpty(fullNameField.getText())||(item.getOrganization()!=null&&check(item.getOrganization().getFullName(), fullNameField.getText())))
                .filter(item->  checkEmpty(annualTurnoverField.getText())||(item.getOrganization()!=null&&check(item.getOrganization().getAnnualTurnover(), annualTurnoverField.getText())))
                .filter(item->  checkEmpty(employeesCountField.getText())||(item.getOrganization()!=null&&check(item.getOrganization().getEmployeesCount(), employeesCountField.getText())))
                .filter(item->  checkEmpty(typeField.getSelectedItem())||(item.getOrganization()!=null&&check(item.getOrganization().getType(), typeField.getSelectedItem())))
                ;

    }

    @Override
    protected void constructLayouts() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        add(clearFilter);
        setColumnSize(clearFilter, "Action");
        addTextFilter(idField, "id");
        addTextFilter(userField, "user");
        addTextFilter(nameField, "name");
        addTextFilter(xField, "coordinates.x");
        addTextFilter(yField, "coordinates.y");
        addTextFilter(creationDateField, "creationDate");
        addTextFilter(salaryField, "salary");
        addTextFilter(startDateField, "startDate");
        addComboFilter(positionField, "position");
        addComboFilter(statusField, "status");
        addTextFilter(fullNameField, "organization.fullName");
        addTextFilter(annualTurnoverField, "organization.annualTurnover");
        addTextFilter(employeesCountField, "organization.employeesCount");
        addComboFilter(typeField, "organization.type");
        add(Layouts.hspace(1000));

    }

    private void setColumnSize(Component component, String columnName) {
        TableColumn column = tablePanel.getTable().getColumn(columnName);
        Dimension dimension1 = new Dimension(column.getMinWidth(), 30);
        Dimension dimension2 = new Dimension(column.getPreferredWidth(), 30);
        Dimension dimension3 = new Dimension(column.getMaxWidth(), 30);

        component.setPreferredSize(dimension2);
        component.setSize(dimension2);
        component.setMinimumSize(dimension1);
        component.setMaximumSize(dimension3);
    }

    private void addTextFilter(JTextField filter, String columnName) {
        add(filter);
        setColumnSize(filter, columnName);
        filter.getDocument().addDocumentListener(new FilterListener(() -> {
            tablePanel.refreshFilter();
            ApplicationFrame.logger.debug("Refreshing filter");
        }));
    }

    private <T> void addComboFilter(JComboBox<T> filter, String columnName) {
        add(filter);
        setColumnSize(filter, columnName);
        filter.addActionListener(event -> tablePanel.refreshFilter());
    }

    protected void updateSize() {
        setColumnSize(clearFilter, "Action");
        setColumnSize(idField, "id");
        setColumnSize(userField, "user");
        setColumnSize(nameField, "name");
        setColumnSize(xField, "coordinates.x");
        setColumnSize(yField, "coordinates.y");
        setColumnSize(creationDateField, "creationDate");
        setColumnSize(salaryField, "salary");
        setColumnSize(startDateField, "startDate");
        setColumnSize(positionField, "position");
        setColumnSize(statusField, "status");
        setColumnSize(fullNameField, "organization.fullName");
        setColumnSize(annualTurnoverField, "organization.annualTurnover");
        setColumnSize(employeesCountField, "organization.employeesCount");
        setColumnSize(typeField, "organization.type");
    }


    @Override
    protected void addListeners() {
        clearFilter.addActionListener(e -> {
            idField.setText("");
            userField.setText("");
            nameField.setText("");
            xField.setText("");
            yField.setText("");
            creationDateField.setText("");
            salaryField.setText("");
            startDateField.setText("");
            positionField.setSelectedIndex(0);
            statusField.setSelectedIndex(0);
            fullNameField.setText("");
            annualTurnoverField.setText("");
            employeesCountField.setText("");
            typeField.setSelectedIndex(0);
        });
    }

    @Override
    public void updateLocale() {
        clearFilter.setText(bundle.getString("clearFilter"));
    }

    @Override
    public void block() {
        clearFilter.setEnabled(false);
        idField.setEnabled(false);
        userField.setEnabled(false);
        nameField.setEnabled(false);
        xField.setEnabled(false);
        yField.setEnabled(false);
        creationDateField.setEnabled(false);
        salaryField.setEnabled(false);
        startDateField.setEnabled(false);
        positionField.setEnabled(false);
        statusField.setEnabled(false);
        fullNameField.setEnabled(false);
        annualTurnoverField.setEnabled(false);
        employeesCountField.setEnabled(false);
        typeField.setEnabled(false);
    }

    @Override
    public void unblock() {
        clearFilter.setEnabled(true);
        idField.setEnabled(true);
        userField.setEnabled(true);
        nameField.setEnabled(true);
        xField.setEnabled(true);
        yField.setEnabled(true);
        creationDateField.setEnabled(true);
        salaryField.setEnabled(true);
        startDateField.setEnabled(true);
        positionField.setEnabled(true);
        statusField.setEnabled(true);
        fullNameField.setEnabled(true);
        annualTurnoverField.setEnabled(true);
        employeesCountField.setEnabled(true);
        typeField.setEnabled(true);
    }

    private static class FilterListener implements DocumentListener {
        private final Runnable runnable;

        public FilterListener(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            runnable.run();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            runnable.run();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            runnable.run();
        }
    }
}
