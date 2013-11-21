package ru.fizteh.fivt.students.demidov.junit;

import java.io.IOException;
import java.util.List;

import ru.fizteh.fivt.students.demidov.basicclasses.BasicDataBaseState;

public class DataBaseState extends BasicDataBaseState<String, TableImplementation> {
	public DataBaseState(TableProviderImplementation provider) {
		super(provider);
	}
	
	public void use(String tableName) throws IOException {
		if (usedTable != null) {
			int changesNumber = usedTable.getChangesNumber();
			if (usedTable.getChangesNumber() > 0) {
				throw new IOException(changesNumber + " unsaved changes");
			}
		}
		TableImplementation gotTable = null;
		try {
			gotTable = provider.getTable(tableName);
		} catch(IllegalArgumentException catchedException) {
			throw new IOException(catchedException);
		}
		if (gotTable == null) {
			throw new IOException(tableName + " not exists");
		}
		usedTable = gotTable;	
	}
	
	public void create(String tableName) throws IOException {
		TableImplementation createdTable = null;
		try {
			createdTable = provider.createTable(tableName);		
		} catch(IllegalArgumentException catchedException) {
			throw new IOException(catchedException);
		} catch(IllegalStateException catchedException) {
			throw new IOException(catchedException);
		}
		if (createdTable == null) {
			throw new IOException(tableName + " exists");
		}
	}	
	
	public String get(String key) throws IOException {
		return getUsedTable().get(key);
	}

	public String put(String key, String value) throws IOException {
		return getUsedTable().put(key, value);
	}

	public String remove(String key) throws IOException {
		return getUsedTable().remove(key);
	}

	public void create(String name, List<Class<?>> columnTypes) throws IOException {}
}
