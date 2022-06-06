package application.model.data.worker;

import application.model.data.exceptions.NullDataException;
import com.sun.istack.internal.NotNull;

import java.io.Serializable;

public class Coordinates implements Serializable {
    private Long x; //Поле не может быть null
    private int y;


    public Coordinates() {
    }

    public Coordinates(Long x, Integer y) {
        this.x = x;
        this.y = y;
    }

    public void setX(@NotNull Long x) {
        if (x == null) throw new NullDataException();
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Long getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}