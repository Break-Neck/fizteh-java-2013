package ru.fizteh.fivt.students.vyatkina.database;


import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;

import java.io.Closeable;
import java.util.Map;

public interface StorableTable extends Table, Closeable {

    int unsavedChanges();

    void putValuesFromDisk(Map<String, Storeable> diskValues);

    void useTransantion(int id);

    void retrieveThreadTable();

    void removeTransaction(int id);

    boolean isClosed();

}
