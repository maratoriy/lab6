package application.model.data.worker;

import application.model.collection.adapter.CollectionItemAdapter;
import application.model.collection.adapter.valuetree.Value;
import application.model.collection.adapter.valuetree.ValueGroup;
import application.model.data.exceptions.InvalidDataException;
import application.model.data.exceptions.NullDataException;
import com.sun.istack.internal.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;


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

        valueGroup.setName("Worker");

        //name
        valueGroup.addValueNode(new Value("name",
                this::getName,
                this::setName));

        //coordinates
        coordinates = new Coordinates();
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
        if (name.trim().equals("")) throw new InvalidDataException("Field cannot be empty");
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
}



