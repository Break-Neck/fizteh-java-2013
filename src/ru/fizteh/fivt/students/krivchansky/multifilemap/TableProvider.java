package src.ru.fizteh.fivt.students.krivchansky.multifilemap;

import src.ru.fizteh.fivt.students.krivchansky.filemap.*;

public interface TableProvider {
	Table getTable(String a);
	Table createTable(String a);
	void removeTable(String a);
}