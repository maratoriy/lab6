package application.view.console;

import application.view.datamodels.StringTable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConsoleTable {
    private String tableName;
    private final List<String> titles;
    private final List<Map<String, String>> table;
    private Map<String, String> formatMap;
    private Map<String, Integer> columnsWidth;
    private int tableWidth;

    {
        tableName = "";
    }

    public ConsoleTable(List<String> titles,
                        List<Map<String, String>> table) {
        this.titles = titles;
        this.table = table;
        List<String> stringList = new LinkedList<>();
    }

    public ConsoleTable(StringTable tableLikeObject) {
        this.titles = tableLikeObject.getTitles();
        this.table = tableLikeObject.getTable();
    }

    public ConsoleTable setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    protected void format() {
        formatMap = titles.stream().collect(Collectors.toMap(
                Function.identity(),
                paramName -> " %" + titles.stream().reduce(paramName, (maxParam, param) -> {
                    for (Map<String, String> iter : table)
                        maxParam = (maxParam.length() < iter.get(paramName).length()) ? iter.get(paramName) : maxParam;
                    return maxParam;
                }).length() + "s |"
        ));
        formatMap.put(titles.get(0), "|" + formatMap.get(titles.get(0)));
        columnsWidth = titles.stream().collect(Collectors.toMap(Function.identity(), paramName -> String.format(formatMap.get(paramName), paramName).length()));
        tableWidth = columnsWidth.values().stream().reduce(0, Integer::sum);


    }

    @Override
    public String toString() {
        titles.forEach(title -> table.forEach(row -> {
            if (!row.containsKey(title)) row.put(title, "");
        }));
        format();

        String res = "";

        String border = String.join("", Collections.nCopies(tableWidth, "-"));
        String boldBorder = String.join("", Collections.nCopies(tableWidth, "="));


        if (!tableName.equals("") && tableName.length() < tableWidth - 2) {
            String whiteSpace = String.join("", Collections.nCopies((tableWidth - 2 - tableName.length()) / 2, " "));
            tableName = "|" + whiteSpace + tableName + whiteSpace;
            tableName += (tableWidth - tableName.length() == 2) ? " |" : "|";
            res += boldBorder + "\n";
            res += tableName + "\n";
        }

        res += boldBorder + "\n";

        res += titles.stream().reduce("", (line, paramName) -> line + String.format(formatMap.get(paramName), paramName)) + "\n";
        String delimiter = titles.stream().reduce("",
                (line, paramName) ->
                        line + String.join("", Collections.nCopies(columnsWidth.get(paramName) - 1, "-")) + "+"
        );
        delimiter = delimiter.substring(0, delimiter.length() - 1) + "-";
        res += boldBorder + "\n";
        StringBuilder resBuilder = new StringBuilder(res);
        boolean started = false;
        for (Map<String, String> tableIter : table) {
            if (started)
                resBuilder.append(delimiter).append("\n");
            else
                started = true;
            resBuilder.append(titles.stream().reduce("", (line, paramName) -> line + String.format(formatMap.get(paramName), tableIter.get(paramName)))).append("\n");
        }
        res = resBuilder.toString();
        res += border + "\n";

        return res;
    }
}
