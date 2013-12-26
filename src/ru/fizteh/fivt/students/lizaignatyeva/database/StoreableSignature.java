package ru.fizteh.fivt.students.lizaignatyeva.database;

import java.util.List;

public class StoreableSignature {
    public final List<Class<?>> columnClasses;

    public StoreableSignature(List<Class<?>> columnClasses) {
        this.columnClasses = columnClasses;
    }

    public Class<?> getColumnClass(int columnIndex) {
        return columnClasses.get(columnIndex);
    }

    public int getColumnsCount() {
        return columnClasses.size();
    }
}
