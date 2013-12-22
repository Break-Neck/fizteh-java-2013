package ru.fizteh.fivt.students.krivchansky.storable;

import java.io.File;
import java.text.ParseException;
import java.util.Set;

import ru.fizteh.fivt.students.krivchansky.filemap.GlobalUtils;
import ru.fizteh.fivt.students.krivchansky.filemap.TableBuilder;
import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;

public class StoreableTableBuilder implements TableBuilder {
	DatabaseTableProvider provider;
	DatabaseTable table;
	
	private int currentDir;
	private int currentFile;
	
	public StoreableTableBuilder(DatabaseTableProvider provider, DatabaseTable table) {
        this.provider = provider;
        this.table = table;
    }
	
	public String get(String key) {
		Storeable val = table.get(key);
        try {
            String presentation = provider.serialize(table, val);
            return presentation;
        } catch (ColumnFormatException e) {
            return null;
        }
	}

	public void put(String key, String value) {
		GlobalUtils.checkKeyPlacement(key, currentDir, currentFile);

        Storeable objectValue = null;
        try {
            objectValue = provider.deserialize(table, value);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
        }
        table.put(key, objectValue);
		
	}

	public Set<String> getKeys() {
		return table.rawGetKeys();
	}

	public File getTableDirectory() {
		return new File(table.getParentDirectory(), table.getName());
	}

	public void setCurrentFile(File curFile) {
		currentDir = GlobalUtils.parseDirNumber(curFile.getParentFile());
        currentFile = GlobalUtils.parseFileNumber(curFile);
		
	}
	
}
