package ru.fizteh.fivt.students.krivchansky.multifilemap;

import ru.fizteh.fivt.students.krivchansky.filemap.*;
import ru.fizteh.fivt.students.krivchansky.shell.SomethingIsWrongException;

public interface TableProvider {
	Table getTable(String a) throws SomethingIsWrongException;
	Table createTable(String a) throws SomethingIsWrongException;
	void removeTable(String a) throws SomethingIsWrongException;
}