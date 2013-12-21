package ru.fizteh.fivt.students.krivchansky.multifilemap;

import ru.fizteh.fivt.storage.strings.TableProvider;
import ru.fizteh.fivt.students.krivchansky.filemap.MyTable;

public interface MyTableProvider extends TableProvider{
	MyTable getTable(String a);
	MyTable createTable(String a);
	void removeTable(String a);
}