package ru.fizteh.fivt.students.zhidkovanton.parallel;

import org.json.JSONArray;
import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;

public class JSONCommands {

    public static String serialize(Table table, Storeable storeable) throws ColumnFormatException {
        try {
            storeable.getColumnAt(table.getColumnsCount());
            throw new ColumnFormatException("Too many columns!");
        } catch (IndexOutOfBoundsException e) {
            //nothing to do
        }

        JSONArray json = new JSONArray();
        for (int i = 0; i < table.getColumnsCount(); ++i) {
            try {
                if (storeable.getColumnAt(i) == null || storeable.getColumnAt(i).getClass() == table.getColumnType(i)) {
                    json.put(storeable.getColumnAt(i));
                } else {
                    throw new ColumnFormatException("Column " + i + " has wrong type!");
                }
            } catch (IndexOutOfBoundsException e) {
                throw new ColumnFormatException("Too few columns!");
            }
        }
        return json.toString();
    }

    public static Storeable deserialize(Table table, String value) {
        if (value == null) {
            return null;
        }
        Storeable storeable = new DataStoreable(table);
        JSONArray array = new JSONArray(value);
        for (Integer i = 0; i < array.length(); ++i) {
            storeable.setColumnAt(i, array.get(i));
        }
        return storeable;
    }
}
