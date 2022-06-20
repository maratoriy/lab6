package application.model.data.worker;


import application.model.data.exceptions.InvalidDataException;
import application.model.data.exceptions.NullDataException;
import com.sun.istack.internal.NotNull;

import java.io.Serializable;

public class Organization implements Serializable {
    private String fullName;       //Длина строки не должна быть больше 662, Значение этого поля должно быть уникальным, Поле может быть null
    private int annualTurnover;    //Значение поля должно быть больше 0
    private Long employeesCount;   //Поле не может быть null, Значение поля должно быть больше 0
    private OrganizationType type; //Поле не может быть null

    public void setFullName(String fullName) {
        if (fullName != null && fullName.length() > 662) throw new InvalidDataException("length more than 662");
        oldFullName = this.fullName;
        this.fullName = fullName;
    }

    public void setAnnualTurnover(int annualTurnover) {
        if (annualTurnover <= 0) throw new InvalidDataException("less or equal than 0");
        this.annualTurnover = annualTurnover;
    }

    public void setEmployeesCount(@NotNull Long employeesCount) {
        if (employeesCount == null) throw new NullDataException();
        if (employeesCount <= 0) throw new InvalidDataException("less or equal than 0");
        this.employeesCount = employeesCount;
    }

    public void setType(@NotNull OrganizationType type) {
        if (type == null) throw new NullDataException();
        this.type = type;
    }

    private String oldFullName;

    public String getOldFullName() {
        return oldFullName;
    }

    public String getFullName() {
        return fullName;
    }

    public int getAnnualTurnover() {
        return annualTurnover;
    }

    public Long getEmployeesCount() {
        return employeesCount;
    }

    public OrganizationType getType() {
        return type;
    }

}
