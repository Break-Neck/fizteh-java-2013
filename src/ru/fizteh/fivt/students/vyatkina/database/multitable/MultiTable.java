package ru.fizteh.fivt.students.vyatkina.database.multitable;

import ru.fizteh.fivt.storage.strings.Table;
import ru.fizteh.fivt.students.vyatkina.database.StringTable;
import ru.fizteh.fivt.students.vyatkina.database.superior.SuperTable;
import ru.fizteh.fivt.students.vyatkina.database.superior.TableChecker;

import java.util.Map;
import java.util.Set;

public class MultiTable extends SuperTable <String> implements StringTable {

    private MultiTableProvider tableProvider;

    public MultiTable (String name, MultiTableProvider tableProvider) {
        super (name);
        this.tableProvider = tableProvider;
    }

    @Override
    public int commit () {
        tableProvider.commitTable (this);
        int savedChanges = super.commit ();
        return savedChanges;
    }
}
