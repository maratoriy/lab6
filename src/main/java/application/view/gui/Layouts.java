package application.view.gui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.stream.Stream;

public class Layouts {
    public static Component space(int width, int height) {
        return Box.createRigidArea(new Dimension(width, height));
    }

    public static Component hspace(int width) {
        return space(width, 0);
    }

    public static Component vspace(int height) {
        return space(0, height);
    }

    public static JComboBox<String> createComboEnum(Object[] objects) {
        ArrayList<String> arrayList = Stream.of(objects).collect(
                ArrayList::new,
                (list, item) -> list.add(item.toString()),
                ArrayList::addAll
        );
        arrayList.add(0, "");
        return new JComboBox<>(arrayList.toArray(new String[0]));
    }
}
