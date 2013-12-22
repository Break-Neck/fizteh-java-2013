package ru.fizteh.fivt.students.baldindima.junit;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import ru.fizteh.fivt.storage.structured.TableProviderFactory;
import ru.fizteh.fivt.storage.structured.TableProvider;
public class MyTableProviderFactory implements TableProviderFactory{
	private volatile boolean isClosed = false;
	private Set<DataBaseTable> tables = new HashSet<>();
	public TableProvider create(String directory) throws IOException{
		if ((directory == null) || directory.trim().equals("")) {
			throw new IllegalArgumentException(" Directory cannot be null");
		}
		File directoryFile = new File(directory);
		if (!directoryFile.exists()){
			if (!directoryFile.mkdir()){
				throw new IOException("Cannot create such directory " + directoryFile.getCanonicalPath());
				
					
			}
		}
		if (!directoryFile.isDirectory()){
			throw new IllegalArgumentException("Wrong directory");
		}
		DataBaseTable tableOfBases = new DataBaseTable(directory);
		tables.add(tableOfBases);
		return tableOfBases;
	}
	public void close() {
        if (!isClosed) {
            for (DataBaseTable table: tables) {
                table.close();
            }
            isClosed = true;
        }
    }
	

}
