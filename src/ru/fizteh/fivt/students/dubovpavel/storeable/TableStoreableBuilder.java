package ru.fizteh.fivt.students.dubovpavel.storeable;

import ru.fizteh.fivt.students.dubovpavel.multifilehashmap.DataBaseBuilder;
import ru.fizteh.fivt.students.dubovpavel.strings.WrappedMindfulDataBaseMultiFileHashMapBuilder;

import java.util.ArrayList;

public class TableStoreableBuilder extends WrappedMindfulDataBaseMultiFileHashMapBuilder {
    private ArrayList<Class<?>> fields;
    public void setFields(ArrayList<Class<?>> types) {
        fields = types;
    }
    public TableStoreable construct() {
        return new TableStoreable(dir, dispatcher, fields);
    }
}
