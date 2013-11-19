package ru.fizteh.fivt.students.krivchansky.multifilemap;
import java.io.IOException;


import ru.fizteh.fivt.students.krivchansky.filemap.*;



public class MultiFileMapShellState extends FileMapShellState implements MultifileMapShellStateInterface<Table, String, String> {
	    public TableProvider tableProvider;

		public Table useTable(String name) {
			table = tableProvider.getTable(name);
			return table;
		}

		public Table createTable(String args) {
			//
			return tableProvider.createTable(args);
		}

		public void dropTable(String name) throws IOException {
			tableProvider.removeTable(name);
		}

		public String getCurrentTableName() {
			return table.getName();
		}
	    
	    
}
