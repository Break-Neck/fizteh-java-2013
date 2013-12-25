package ru.fizteh.fivt.students.zhidkovanton.JUnit;

import ru.fizteh.fivt.storage.strings.Table;
import ru.fizteh.fivt.storage.strings.TableProvider;


public class DataBaseFactory {
    public Table dataBase;
    public TableProvider dataFactory;

    public DataBaseFactory(TableProvider provider) {
        dataFactory = provider;
        dataBase = null;
    }
}
