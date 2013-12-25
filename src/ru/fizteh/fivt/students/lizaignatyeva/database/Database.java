package ru.fizteh.fivt.students.lizaignatyeva.database;

import ru.fizteh.fivt.storage.structured.Storeable;

import java.text.ParseException;

public class Database {
    public MyTable currentTable;
    public MyTableProvider tableProvider;

    public Database(MyTableProvider tableProvider) {
        this.tableProvider = tableProvider;
        this.currentTable = null;
    }

    public String serialize(Storeable storeable) {
        return tableProvider.serialize(currentTable, storeable);
    }

    public Storeable deserialize(String value) throws ParseException {
        return tableProvider.deserialize(currentTable, value);
    }

    public boolean checkActive() {
        boolean result = currentTable != null;
        if (!result) {
            System.out.println("no table");
        }
        return result;
    }
}
