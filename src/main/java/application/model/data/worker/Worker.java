package application.model.data.worker;

import application.model.collection.adapter.CollectionItemAdapter;
import application.model.collection.adapter.valuetree.Value;
import application.model.collection.adapter.valuetree.ValueGroup;
import application.model.collection.database.DBCollectionItem;
import application.model.collection.database.DBRequest;
import application.model.collection.database.Database;
import application.model.data.exceptions.EmptyFieldException;
import application.model.data.exceptions.InvalidDataException;
import application.model.data.exceptions.NullDataException;
import com.sun.istack.internal.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class Worker extends CollectionItemAdapter<Worker> {

    private long id; //Поле не может быть null, Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    private String name; //Поле не может быть null, Строка не может быть пустой
    private final Coordinates coordinates; //Поле не может быть null
    private LocalDateTime creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически
    private Double salary; //Значение поля должно быть больше 0
    private LocalDate startDate; //Поле не может быть null
    private Position position; //Поле может быть null
    private Status status; //Поле может быть null
    private Organization organization; //Поле не может быть null

    {
        creationDate = LocalDateTime.now();
    }


    public Worker(Long id) {
        super(id);
        coordinates = new Coordinates();
        setupValueTree();
    }

    @Override
    public void setupValueTree() {
        super.setupValueTree();


        valueGroup.setName("Worker");

        //name
        valueGroup.addValueNode(new Value("name",
                this::getName,
                this::setName));

        //coordinates
        ValueGroup coordinatesValueGroup = new ValueGroup("coordinates");
        coordinatesValueGroup.addValueNode(new Value("x",
                () -> this.getCoordinates().getX().toString(),
                (str) -> this.getCoordinates().setX(Long.valueOf(str))));
        coordinatesValueGroup.addValueNode(new Value("y",
                () -> String.valueOf(this.getCoordinates().getY()),
                (str) -> this.getCoordinates().setY(Integer.parseInt(str))));
        valueGroup.addValueNode(coordinatesValueGroup);

        //creationDate
        valueGroup.addValueNode(new Value("creationDate",
                () -> this.getCreationDate().toString(),
                (str) -> this.setCreationDate(LocalDateTime.parse(str)))
                .block()
        );

        //salary
        valueGroup.addValueNode(new Value("salary",
                () -> this.getSalary().toString(),
                (str) -> this.setSalary(Double.valueOf(str))));

        //startDate
        valueGroup.addValueNode(new Value("startDate",
                () -> this.getStartDate().toString(),
                (str) -> this.setStartDate(LocalDate.parse(str))));

        //position
        valueGroup.addValueNode(new Value("position",
                () -> this.getPosition().toString(),
                (str) -> this.setPosition(str == null ? null : Position.valueOf(str))));

        //status
        valueGroup.addValueNode(new Value("status",
                () -> this.getStatus().toString(),
                (str) -> this.setStatus(str == null ? null : Status.valueOf(str))));

        //organization
        ValueGroup organizationValueGroup = new ValueGroup("organization", () -> organization == null, () -> setOrganization(new Organization()));
        organizationValueGroup.addValueNode(new Value("fullName",
                () -> getOrganization().getFullName(),
                (str) -> getOrganization().setFullName(str))
                .setUnique());
        organizationValueGroup.addValueNode(new Value("annualTurnover",
                () -> String.valueOf(getOrganization().getAnnualTurnover()),
                (str) -> getOrganization().setAnnualTurnover(Integer.parseInt(str))));
        organizationValueGroup.addValueNode(new Value("employeesCount",
                () -> getOrganization().getEmployeesCount().toString(),
                (str) -> getOrganization().setEmployeesCount(Long.valueOf(str))));
        organizationValueGroup.addValueNode(new Value("type",
                () -> getOrganization().getType().toString(),
                (str) -> getOrganization().setType(OrganizationType.valueOf(str))));
        valueGroup.addValueNode(organizationValueGroup);
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(@NotNull Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        if (name == null) throw new NullDataException();
        if (name.trim().equals("")) throw new EmptyFieldException();
        this.name = name;
    }


    public Coordinates getCoordinates() {
        return coordinates;
    }


    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(@NotNull LocalDateTime creationDate) {
        if (creationDate == null) throw new NullDataException();
        this.creationDate = creationDate;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(@NotNull Double salary) {
        if (salary == null) throw new NullDataException();
        if (salary <= 0) throw new InvalidDataException("less or equal 0");
        this.salary = salary;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(@NotNull LocalDate startDate) {
        if (startDate == null) throw new NullDataException();
        this.startDate = startDate;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    @Override
    public List<DBRequest> deleteAll(String db_name, String user) {
        List<DBRequest> deleteAll = super.deleteAll(db_name, user);
        deleteAll.add(DBCollectionItem.deleteAllByUser("organizations", user));
        return deleteAll;
    }

    @Override
    public List<DBRequest> deleteAllCompletely(String db_name) {
        List<DBRequest> deleteAll = super.deleteAllCompletely(db_name);
        deleteAll.add(DBCollectionItem.deleteAll("organizations"));
        return deleteAll;
    }

    @Override
    public List<DBRequest> insert(String db_name) {
        List<DBRequest> dbRequests = new ArrayList<>();
        String sql = "INSERT INTO " + db_name + " VALUES(nextVal('nextVal'),?,?,?,?,?,?,?,?,?,?)";
        String fullName = (organization != null) ? organization.getFullName() : null;
        DBRequest insertWorker = new DBRequest(sql,
                getUser(),
                getName(),
                getCoordinates().getX(),
                getCoordinates().getY(),
                getCreationDate().toString(),
                getSalary(),
                getStartDate(),
                getPosition().toString(),
                getStatus().toString(),
                fullName
        );
        dbRequests.add(insertWorker);
        if (organization != null) {
            String sqlOrganization = "INSERT INTO " + "organizations" + " VALUES(?,?,?,?,?)";
            DBRequest insertOrganization = new DBRequest(sqlOrganization,
                    organization.getFullName(),
                    getUser(),
                    organization.getAnnualTurnover(),
                    organization.getEmployeesCount(),
                    organization.getType());
            dbRequests.add(insertOrganization);
        }

        return dbRequests;
    }

    private static String createLine(String valueName) {
        return String.format("\"%s\" = ?, ", valueName);
    }

    private static String createEndLine(String valueName) {
        return String.format("\"%s\" = ? ", valueName);
    }


    @Override
    public List<DBRequest> update(String db_name) {
        String where = "WHERE \"id\" = ?";
        List<DBRequest> dbRequests = new ArrayList<>();
        String sql = "UPDATE " + db_name + " " +
                "SET " +
                createLine("id") +
                createLine("user") +
                createLine("name") +
                createLine("x") +
                createLine("y") +
                createLine("creationDate") +
                createLine("salary") +
                createLine("startDate") +
                createLine("position") +
                createLine("status") +
                createEndLine("organizationFullName") +
                where;

        String oldFullName = (organization == null) ? null : organization.getOldFullName();
        String fullName = (organization != null) ? organization.getFullName() : null;
        DBRequest updateWorker = new DBRequest(sql,
                getId(),
                getUser(),
                getName(),
                coordinates.getX(),
                coordinates.getY(),
                getCreationDate().toString(),
                getSalary(),
                getStartDate(),
                getPosition().toString(),
                getStatus().toString(),
                fullName,
                getId());
        dbRequests.add(updateWorker);
        if (organization != null) {
            String sqlOrganization = "UPDATE organizations SET " +
                    createLine("fullName") +
                    createLine("user") +
                    createLine("annualTurnover") +
                    createLine("employeesCount") +
                    createEndLine("type") +
                    "WHERE \"fullName\" = ? ";
            DBRequest dbRequest = new DBRequest(sqlOrganization,
                    organization.getFullName(),
                    getUser(),
                    organization.getAnnualTurnover(),
                    organization.getEmployeesCount(),
                    organization.getType().toString(),
                    oldFullName
            );
            dbRequests.add(dbRequest);
        }
        return dbRequests;
    }

    @Override
    public List<DBRequest> delete(String db_name) {
        List<DBRequest> dbRequests = super.delete(db_name);
        if (organization != null) {
            String fullName = organization.getFullName();
            String deleteOrganization = "DELETE FROM organizations WHERE \"fullName\" = ? ";
            DBRequest dbRequest = new DBRequest(deleteOrganization, fullName);
            dbRequests.add(dbRequest);
        }
        return dbRequests;
    }

    @Override
    public void parse(ResultSet resultSet) throws SQLException {
        setId(resultSet.getLong("id"));
        setUser(resultSet.getString("user"));
        setName(resultSet.getString("name"));
        getCoordinates().setX(resultSet.getLong("x"));
        getCoordinates().setY(resultSet.getInt("y"));
        setCreationDate(LocalDateTime.parse(resultSet.getString("creationDate")));
        setSalary(resultSet.getDouble("salary"));
        setStartDate(resultSet.getDate("startDate").toLocalDate());
        setPosition(Position.valueOf(resultSet.getString("position")));
        setStatus(Status.valueOf(resultSet.getString("status")));

        String fullName = resultSet.getString("organizationFullName");
        if (fullName != null) {
            organization = new Organization();
            ResultSet orgResSet = Database.getInstance().executeQuery("SELECT * FROM organizations WHERE \"fullName\" = ?", fullName);
            if (!orgResSet.next()) return;
            organization.setFullName(fullName);
            organization.setAnnualTurnover(orgResSet.getInt("annualTurnover"));
            organization.setEmployeesCount(orgResSet.getLong("employeesCount"));
            organization.setType(OrganizationType.valueOf(orgResSet.getString("type")));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Worker worker = (Worker) o;
        return id == worker.id && name.equals(worker.name) && coordinates.equals(worker.coordinates) && creationDate.equals(worker.creationDate) && salary.equals(worker.salary) && startDate.equals(worker.startDate) && position == worker.position && status == worker.status;// && Objects.equals(organization, worker.organization);
    }

}



