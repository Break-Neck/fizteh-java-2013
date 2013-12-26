package ru.fizteh.fivt.students.zhidkovanton.storable;

import ru.fizteh.fivt.storage.structured.Table;
import ru.fizteh.fivt.storage.structured.TableProvider;


public class DataBaseFactory {
    public Table dataBase;
    public TableProvider dataFactory;

    public DataBaseFactory(TableProvider provider) {
        dataFactory = provider;
        dataBase = null;
    }
}
