package application.model.data.worker;

import application.model.data.exceptions.NullDataException;
import com.sun.istack.internal.NotNull;

import java.io.Serializable;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinates that = (Coordinates) o;
        return y == that.y && Objects.equals(x, that.x);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}