package application.view;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface StringTable extends Serializable {
    static Map<String, String> stringMatrixToMap(String[][] matrix) {
        return Stream.of(matrix).collect(Collectors.toMap(data -> data[0], data -> data[1]));
    }

    List<String> getTitles();

    List<Map<String, String>> getTable();
}
