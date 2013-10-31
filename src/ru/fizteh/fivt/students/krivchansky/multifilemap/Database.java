package ru.fizteh.fivt.students.krivchansky.multifilemap;

import ru.fizteh.fivt.students.krivchansky.filemap.*;
import ru.fizteh.fivt.students.krivchansky.shell.*;
import javax.swing.plaf.multi.MultiInternalFrameUI;
import java.io.File;
import java.util.HashMap;

public class Database implements TableProvider {
    HashMap<String, MultifileTable> content = new HashMap<String, MultifileTable>();
    private String databaseDirectoryPath;

    public Database(String databaseDirectoryPath) {
        this.databaseDirectoryPath = databaseDirectoryPath;
        File databaseDirectory = new File(databaseDirectoryPath);
        for(File tableFile : databaseDirectory.listFiles()) {
            	if (tableFile == null || tableFile.isFile()) {
                	continue;
            	}
            	MultifileTable table = new MultifileTable(databaseDirectoryPath, tableFile.getName());
            	content.put(table.getName(), table);
        	}
    }

    public MultifileTable getTable(String name) throws SomethingIsWrongException {
        if (name == null) {
            throw new SomethingIsWrongException("Table's name cannot be null");
        }
        MultifileTable table = content.get(name);

        if (table == null) {
            throw new SomethingIsWrongException("Tablename does not exist");
        }
        if (table.getChangesCounter() > 0 && !table.getAutoCommit()) {
            throw new SomethingIsWrongException(table.getChangesCounter() + " uncommited changes");
        }
        table.setAutoCommit(true); //here if you want to open with autocommit option
        return table;
    }

    public MultifileTable createTable(String name) throws SomethingIsWrongException {
        if (name == null) {
            throw new IllegalArgumentException("Table's name cannot be null");
        }
        if (content.containsKey(name)) {
            throw new IllegalStateException(name + " exists");
        }
        MultifileTable table = new MultifileTable(databaseDirectoryPath, name);
        table.setAutoCommit(true);  //here you can change if you need autocommit on use/exit or not 
        content.put(name, table);
        return table;
    }

    public void removeTable(String name) throws SomethingIsWrongException {
        if (name == null) {
            throw new SomethingIsWrongException("Table's name cannot be null");
        }

        if (!content.containsKey(name)) {
            throw new SomethingIsWrongException(name + " not exists");
        }
        content.remove(name);
        File tableFile = new File(databaseDirectoryPath, name);
        if (tableFile.list().length != 0 ) {
        	for (String temp : tableFile.list() ) {
        		File toDel = new File (tableFile.getAbsolutePath(), temp);
        		if (toDel.list().length != 0 ) {
        			for (String innerTemp : toDel.list()) {
        				File innerToDel = new File (toDel.getAbsolutePath(), innerTemp);
        				innerToDel.delete();
        			}
        		}
        		toDel.delete();
        	}
        }
        tableFile.delete();
        
    }
}
