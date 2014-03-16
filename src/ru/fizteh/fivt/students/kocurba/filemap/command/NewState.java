package ru.fizteh.fivt.students.kocurba.filemap.command;

import ru.fizteh.fivt.storage.strings.Table;
import ru.fizteh.fivt.storage.strings.TableProvider;

/**
 * Created with IntelliJ IDEA.
 * User: Alina
 * Date: 15.03.14
 * Time: 20:04
 * To change this template use File | Settings | File Templates.
 */
public class NewState {
    private Table currentTable;
    private TableProvider tableProvider;

    public NewState(TableProvider tableProvider, Table currentTable) {
        this.tableProvider = tableProvider;
        this.currentTable = currentTable;
    }

    public NewState(TableProvider tableProvider) {
        this(tableProvider, null);
    }

    public Table getCurrentTable() {
        return this.currentTable;
    }

    public TableProvider getTableProvider() {
        return this.tableProvider;
    }

    public void setCurrentTable(Table newTable) {
        this.currentTable = newTable;
    }
}
