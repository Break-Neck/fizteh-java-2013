package ru.fizteh.fivt.students.krivchansky.multifilemap;
import java.io.IOException;

import ru.fizteh.fivt.students.krivchansky.filemap.FileMapShellState;
import ru.fizteh.fivt.students.krivchansky.filemap.MyTable;
import ru.fizteh.fivt.storage.strings.TableProvider;

public class MultiFileMapShellState extends FileMapShellState implements MultifileMapShellStateInterface<MyTable, String, String> {
	    public TableProvider tableProvider;

		public MyTable useTable(String name) {
			MyTable tempTable = (MyTable) tableProvider.getTable(name);
			return tempTable;
		}

		public MyTable createTable(String args) {
			return (MyTable) tableProvider.createTable(args);
		}

		public void dropTable(String name) throws IOException {
			tableProvider.removeTable(name);
		}

		public String getCurrentTableName() {
			return table.getName();
		}
	    
	    
}
