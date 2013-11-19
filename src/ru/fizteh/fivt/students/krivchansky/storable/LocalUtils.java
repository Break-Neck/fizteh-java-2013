package ru.fizteh.fivt.students.krivchansky.storable;


import java.util.ArrayList;
import java.util.List;

import ru.fizteh.fivt.students.krivchansky.shell.Parser;

public class LocalUtils {
	public static List<Object> parseValues(List<String> valuesRepresentation, Table table) throws ColumnFormatException {
        List<Object> values = new ArrayList<>(valuesRepresentation.size() - 1);

        for (int index = 1; index < valuesRepresentation.size(); ++index) {
            Object value = StoreableTypes.parseByClass(valuesRepresentation.get(index), table.getColumnType(index - 1));
            values.add(value);
        }
        return values;
    }

    public static String join(List<?> list) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (final Object listEntry : list) {
            if (!first) {
                sb.append(" ");
            }
            first = false;
            if (listEntry == null) {
                sb.append("null");
            } else {
                sb.append(listEntry.toString());
            }
        }
        return sb.toString();
    }

    public static TableInfo parseCreateCommand(String parameters) throws IllegalArgumentException {
        parameters = parameters.trim();
        String tableName = parameters.split("\\s+")[0];
        parameters = parameters.replaceAll("\\s+", " ");
        int spaceIndex = parameters.indexOf(' ');
        if (spaceIndex == -1) {
            throw new IllegalArgumentException("wrong type (no column types)");
        }
        String columnTypesString = parameters.substring(spaceIndex).replaceAll("\\((.*)\\)", "$1");
        List<String> columnTypes = Parser.parseCommandArgs(columnTypesString);

        TableInfo info = new TableInfo(tableName);
        for (final String columnType : columnTypes) {
            info.addColumn(StoreableTypes.getTypeByName(columnType));
        }

        return info;
    }

    public static List<String> formatColumnTypes(List<Class<?>> columnTypes) {
        List<String> formattedColumnTypes = new ArrayList<String>();
        for (final Class<?> columnType : columnTypes) {
            formattedColumnTypes.add(StoreableTypes.getSimpleName(columnType));
        }
        return formattedColumnTypes;
    }


    public static boolean checkStringCorrect(String string) {
        return string.matches("\\s*") || string.split("\\s+").length != 1;
    }

}
