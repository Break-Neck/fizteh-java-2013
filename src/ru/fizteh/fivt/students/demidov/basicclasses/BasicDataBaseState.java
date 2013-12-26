package ru.fizteh.fivt.students.demidov.basicclasses;

import java.io.IOException;
import java.util.List;

public abstract class BasicDataBaseState<ElementType, TableType extends BasicTable<ElementType>> implements BasicState {
	protected BasicTableProvider<TableType> provider;	
	protected TableType usedTable;
	
	public BasicDataBaseState(BasicTableProvider<TableType> provider) {
		this.provider = provider;
		usedTable = null;
	}
	
	public TableType getUsedTable() throws IOException {
		if (usedTable == null) {
			throw new IOException("no table");
		}
		return usedTable;
	}
	
	public void drop(String tableName) throws IOException {
		if ((usedTable != null) && (usedTable.getName().equals(tableName))) {
			usedTable = null;
		}
		try {
			provider.removeTable(tableName);
		} catch (IllegalArgumentException catchedException) {
			throw new IOException(catchedException.getMessage());
		} catch (IllegalStateException catchedException) {
			throw new IOException(catchedException.getMessage());
		}
	}
	
	public abstract void use(String tableName) throws IOException;	
	public abstract void create(String name) throws IOException;
	public abstract void create(String name, List<Class<?>> columnTypes) throws IOException;	
	public abstract String get(String key) throws IOException;
	public abstract String put(String key, String value) throws IOException;	
	public abstract String remove(String key) throws IOException;
}
