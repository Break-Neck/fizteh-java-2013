package ru.fizteh.fivt.students.kocurba.filemap.command;

import ru.fizteh.fivt.storage.structured.TableProvider;
import ru.fizteh.fivt.students.kocurba.storeable.StoreableTable;

public class State {

    private StoreableTable currentTable;
    private TableProvider tableProvider;

    public State(TableProvider tableProvider, StoreableTable currentTable) {
        this.tableProvider = tableProvider;
        this.currentTable = currentTable;
    }

    public State(TableProvider tableProvider) {
        this(tableProvider, null);
    }

    public StoreableTable getCurrentTable() {
        return this.currentTable;
    }

    public TableProvider getTableProvider() {
        return this.tableProvider;
    }

    public void setCurrentTable(StoreableTable newTable) {
        this.currentTable = newTable;
    }

}
