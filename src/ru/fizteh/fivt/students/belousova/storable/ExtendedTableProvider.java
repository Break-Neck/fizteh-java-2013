package ru.fizteh.fivt.students.belousova.storable;

import ru.fizteh.fivt.storage.structured.TableProvider;

public interface ExtendedTableProvider extends TableProvider {
    @Override
    ExtendedTable getTable(String name);
}
