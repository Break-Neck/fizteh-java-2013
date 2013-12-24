package ru.fizteh.fivt.students.krivchansky.storable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Set;

import ru.fizteh.fivt.students.krivchansky.filemap.SomeStorage;
import ru.fizteh.fivt.students.krivchansky.multifilemap.MultiFileMapReadingUtils;
import ru.fizteh.fivt.students.krivchansky.multifilemap.MultiFileMapWritingUtils;
import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;

public class DatabaseTable extends SomeStorage<String, Storeable> implements Table {

	DatabaseTableProvider provider;
	private List<Class<?>> columnTypes;
	
	public DatabaseTable(DatabaseTableProvider provider, String databaseDir, String name, List<Class<?>> columnTypes) {
		super(databaseDir, name);
		if (columnTypes == null || columnTypes.isEmpty()) {
			throw new IllegalArgumentException ("column types cannot be null");
		}
		this.columnTypes = columnTypes;
		this.provider = provider;
		try{
			checkTableDirectory();
			load();
		} catch(IOException e) {
			throw new IllegalArgumentException("invalid file format");
		}
	}
	
	public Storeable get(String key) {
        return getFromStorage(key);
    }
	
	public Storeable put(String key, Storeable value) throws ColumnFormatException {
        if (key != null) {
            if (LocalUtils.checkStringCorrect(key)) {
                throw new IllegalArgumentException("key cannot be empty");
            }
        }

        if (value == null) {
            throw new IllegalArgumentException("value cannot be null");
        }

        if (!checkAlienStoreable(value)) {
            throw new ColumnFormatException("alien storeable");
        }
        isStoreableCorrect(value);
        return putIntoStorage(key, value);
    }
	
	 public Storeable remove(String key) {
	        return removeFromStorage(key);
	    }

	    public int size() {
	        return sizeOfStorage();
	    }

	    public int commit() throws IOException {
	        return commitStorage();
	    }

	    public int rollback() {
	        return rollbackStorage();
	    }

	    public int getColumnsCount() {
	        return columnTypes.size();	
	    }
	    
	    public Class<?> getColumnType(int columnIndex) throws IndexOutOfBoundsException {
	        if (columnIndex < 0 || columnIndex > getColumnsCount()) {
	            throw new IndexOutOfBoundsException();
	        }
	        return columnTypes.get(columnIndex);
	    }

	    protected void load() throws IOException {
	        if (provider == null) {
	            return;
	        }
	        MultiFileMapReadingUtils.load(new StoreableTableBuilder(provider, this));
	    }

	    protected void save() throws IOException {
	    	MultiFileMapWritingUtils.save(new StoreableTableBuilder(provider, this));
	    }

	    private void checkTableDirectory() throws IOException {
	        File tableDirectory = new File(getParentDirectory(), getName());
	        if (!tableDirectory.exists()) {
	            tableDirectory.mkdir();
	            writeSignatureFile();
	        } else {
	            File[] children = tableDirectory.listFiles();
	            if (children == null || children.length == 0) {
	                throw new IllegalArgumentException(String.format("table directory: %s is empty", tableDirectory.getAbsolutePath()));
	            }
	        }
	    }

	    private void writeSignatureFile() throws IOException {
	        File tableDirectory = new File(getParentDirectory(), getName());
	        File signatureFile = new File(tableDirectory, DatabaseTableProvider.SIGNATURE_FILE);
	        signatureFile.createNewFile();
	        BufferedWriter writer = new BufferedWriter(new FileWriter(signatureFile));
	        List<String> formattedColumnTypes = LocalUtils.formatColumnTypes(columnTypes);
	        String signature = LocalUtils.join(formattedColumnTypes);
	        writer.write(signature);
	        writer.close();
	    }
	    
	   	    
	    public boolean checkAlienStoreable(Storeable storeable) {
	        for (int index = 0; index < getColumnsCount(); ++index) {
	            try {
	                Object o = storeable.getColumnAt(index);
	                if (o == null) {
	                    continue;
	                }
	                if (!o.getClass().equals(getColumnType(index))) {
	                    return false;
	                }
	            } catch (IndexOutOfBoundsException e) {
	                return false;
	            }
	        }
	        try {
	            storeable.getColumnAt(getColumnsCount());
	        } catch (IndexOutOfBoundsException e) {
	            return true;
	        }
	        return false;
	    }

	    Set<String> rawGetKeys() {
	        return unchangedOldData.keySet();
	    }
	    
	    public void isStoreableCorrect(Storeable storeable) {
	        for (int index = 0; index < getColumnsCount(); ++index) {
	            try {
	                LocalUtils.checkValue(storeable.getColumnAt(index), columnTypes.get(index));
	            } catch (ParseException e) {
	                throw new IllegalArgumentException(e);
	            }
	        }
	    }
}
