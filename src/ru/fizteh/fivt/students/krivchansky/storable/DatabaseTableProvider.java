package ru.fizteh.fivt.students.krivchansky.storable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ru.fizteh.fivt.students.krivchansky.filemap.GlobalUtils;
import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;
import ru.fizteh.fivt.storage.structured.TableProvider;


public class DatabaseTableProvider implements TableProvider {
	static final String SIGNATURE_FILE = "signature.tsv";
	private static final String CHECKER = "[0-9A-Za-zА-Яа-я]+";
	private final Lock tableLock = new ReentrantLock(true);
	
	HashMap<String, DatabaseTable> tables = new HashMap<String, DatabaseTable>();
	private String databaseDirPath;
	private DatabaseTable currentTable = null;
	
	public DatabaseTableProvider(String databaseDirPath) {
		if (databaseDirPath == null) {
			throw new IllegalArgumentException ("path to database can not be null");
		}
		
		this.databaseDirPath = databaseDirPath;
		File databaseDir = new File(databaseDirPath);
		if (databaseDir.isFile()) {
			throw new IllegalArgumentException("database dir cannot be file");
		}
		for (File tableFile : databaseDir.listFiles()) {
			if (tableFile.isFile()) {
				continue;
			}
			List<Class<?>> columnTypes = readTableSignature(tableFile.getName());
			if (columnTypes == null) {
				throw new IllegalArgumentException("table directory is empty");
			}
			DatabaseTable table = new DatabaseTable(this, databaseDirPath, tableFile.getName(), columnTypes);
			tables.put(table.getName(), table);
		}
	}
	
	public Table getTable(String name) {
		try {
			tableLock.lock();
			if (name == null || name.isEmpty()) {
				throw new IllegalArgumentException("table cannot be null");
			}
			checkTableName(name);
			DatabaseTable  table = tables.get(name);
			if (table == null) {
				return null;
			}
			if (currentTable != null && currentTable.getChangesCounter() > 0) {
				throw new IllegalStateException (currentTable.getChangesCounter() + " unsaved changes");
			}
			currentTable = table;
			return table;
		} finally {
			tableLock.unlock();
		}
	}
	
	public Table createTable(String name, List<Class<?>> columnTypes) throws IOException {
		try {
			tableLock.lock();
			if (name == null || name.isEmpty()) {
				throw new IllegalArgumentException("table's name cannot be null");
			}
			checkTableName(name);
			if (columnTypes == null || columnTypes.isEmpty()) {
				throw new IllegalArgumentException("wrong type ()");
			}
			checkColumnTypes(columnTypes);
			if (tables.containsKey(name)) {
                return null;
            }
			DatabaseTable table = new DatabaseTable(this, databaseDirPath, name, columnTypes);
			tables.put(name, table);
			return table;
		} finally {
			tableLock.unlock();
		}
	}
	
	public void removeTable(String name) throws IOException {
		try {
			tableLock.lock();
			if (name == null || name.isEmpty()) {
				throw new IllegalArgumentException("table's name cannot be null");
			}
			
			if (!tables.containsKey(name)) {
				throw new IllegalStateException(name + " not exists");
			}
			tables.remove(name);
			File tableFile = new File(databaseDirPath, name);
			GlobalUtils.deleteFile(tableFile);
		} finally {
			tableLock.unlock();
		}
	}
	
	public String serialize(Table table, Storeable value) throws ColumnFormatException {
        if (value == null) {
            throw new IllegalArgumentException("value cannot be null");
        }
        try {
            XmlSerializer xmlSerializer = new XmlSerializer();
            for (int index = 0; index < table.getColumnsCount(); ++index) {
                xmlSerializer.write(value.getColumnAt(index));
            }
            xmlSerializer.close();
            return xmlSerializer.getRepresentation();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } catch (ParseException e) {
            throw new IllegalArgumentException("incorrect value");
        }
        return null;
    }

	
	public Storeable deserialize(Table table, String val) throws ParseException {
        if (val == null || val.isEmpty()) {
            throw new IllegalArgumentException("value cannot be null or empty");
        }
        XmlDeserializer deserializer = new XmlDeserializer(val);
        Storeable result = null;
        List<Object> values = new ArrayList<>(table.getColumnsCount());
        for (int index = 0; index < table.getColumnsCount(); ++index) {
            try {
                Class<?> expectedType = table.getColumnType(index);
                Object columnValue = deserializer.getNext(expectedType);
                LocalUtils.checkValue(columnValue, expectedType);
                values.add(columnValue);
            } catch (ColumnFormatException e) {
                throw new ParseException("incompatible type: " + e.getMessage(), index);
            } catch (IndexOutOfBoundsException e) {
                throw new ParseException("Xml representation doesn't match the format", index);
            }
        }
        try {
            deserializer.close();
            result = createFor(table, values);
        } catch (ColumnFormatException e) {
            throw new ParseException("incompatible types: " + e.getMessage(), 0);
        } catch (IndexOutOfBoundsException e) {
            throw new ParseException("Xml representation doesn't match the format", 0);
        } catch (IOException e) {
            throw new ParseException(e.getMessage(), 0);
        }
        return result;
    }
	
	public Storeable createFor(Table table) {
        return rawCreateFor(table);
    }
	
	public Storeable createFor(Table table, List<?> values) throws ColumnFormatException, IndexOutOfBoundsException {
        if (values == null) {
            throw new IllegalArgumentException("values cannot be null");
        }
        DatabaseRow row = rawCreateFor(table);
        row.setColumns(values);
        return row;
    }
	
	private DatabaseRow rawCreateFor(Table table) {
        DatabaseRow row = new DatabaseRow();
        for (int index = 0; index < table.getColumnsCount(); ++index) {
            row.addColumn(table.getColumnType(index));
        }
        return row;
    }
	
	
	private List<Class<?>> readTableSignature(String tableName) {
	    File tableDirectory = new File(databaseDirPath, tableName);
	    File signatureFile = new File(tableDirectory, SIGNATURE_FILE);
	    String signature = null;
	    if (!signatureFile.exists()) {
	        return null;
	    }
	    try (BufferedReader reader = new BufferedReader(new FileReader(signatureFile))) {
	        signature = reader.readLine();
	    } catch (IOException e) {
	        System.err.println("error loading signature file: " + e.getMessage());
	        return null;
	    }
        if (signature == null) {
            throw new IllegalArgumentException("incorrect signature file");
	    }
        List<Class<?>> columnTypes = new ArrayList<Class<?>>();
        for (final String columnType : signature.split("\\s+")) {
            Class<?> type = StoreableTypes.getTypeByName(columnType);
            if (type == null) {
                throw new IllegalArgumentException("wrong type (" + columnType + ')');
            }
            columnTypes.add(type);
        }
        return columnTypes;
    }
	 
	private boolean checkCorrectTable(File tableDirectory) {
	     File signatureFile = new File(tableDirectory, SIGNATURE_FILE);
	     return signatureFile.exists();
	}
	
	private void checkColumnTypes(List<Class<?>> columnTypes) {
        for (final Class<?> columnType : columnTypes) {
            if (columnType == null) {
                throw new IllegalArgumentException("wrong type ()");
            }
            StoreableTypes.getSimpleName(columnType);
        }
    }
	
	private void checkTableName(String name) {
        if (!name.matches(CHECKER)) {
            throw new IllegalArgumentException("Bad symbol!");
        }
    }
}
