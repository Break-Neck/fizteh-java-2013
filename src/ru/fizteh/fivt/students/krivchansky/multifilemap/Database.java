package src.ru.fizteh.fivt.students.krivchansky.multifilemap;

import java.io.File;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import src.ru.fizteh.fivt.students.krivchansky.filemap.GlobalUtils;

public class Database implements TableProvider {
	private static final String CHECK_NAME_EXPRESSION = "[^0-9A-Za-zА-Яа-я]+";
    HashMap<String, MultifileTable> content = new HashMap<String, MultifileTable>();
    private String databaseDirectoryPath;
    private MultifileTable currentTable = null;

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

    public MultifileTable getTable(String name) {
        if (name == null) {
            throw new IllegalArgumentException("table's name cannot be null");
        }
        checkName(name);
        MultifileTable table = content.get(name);
        if (table == null) {
            return table;
        }
        if (currentTable != null) {
        	int changes = currentTable.getChangesCounter();
            if (changes > 0 && !currentTable.getAutoCommit()) {
        		throw new IllegalStateException(changes + " unsaved changes");
            } else if (currentTable.getAutoCommit()) {
        	    currentTable.commit();
            }
        }
        table.setAutoCommit(true); //here if you want to start with autocommit option
        currentTable = table;
        return table;
    }

    public MultifileTable createTable(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Table's name cannot be null");
        }
        checkName(name);
        if (content.containsKey(name)) {
            return null;
        }
        File tableDirectory = new File(databaseDirectoryPath, name);
        if (!tableDirectory.exists()) {
        	tableDirectory.mkdir();
        }
        MultifileTable table = new MultifileTable(databaseDirectoryPath, name);
        table.setAutoCommit(true);  //here you can change if you need autocommit on use/exit or not 
        content.put(name, table);
        return table;
    }

    public void removeTable(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException ("Table's name cannot be null");
        }

        if (!content.containsKey(name)) {
            throw new IllegalStateException(name + " not exists");
        }
        content.remove(name);
        File tableFile = new File(databaseDirectoryPath, name);
        GlobalUtils.deleteFile(tableFile);
    }
    
    private void checkName(String name) {
    	Pattern pattern = Pattern.compile(CHECK_NAME_EXPRESSION);
    	Matcher matcher = pattern.matcher(name);
    	if (matcher.find()) {
    		throw new IllegalArgumentException("bad symbol in table's name");
    	}
    }
}
