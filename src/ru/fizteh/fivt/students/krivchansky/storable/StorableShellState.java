package ru.fizteh.fivt.students.krivchansky.storable;

import java.io.IOException;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;
import ru.fizteh.fivt.storage.structured.TableProvider;
import java.text.ParseException;

import ru.fizteh.fivt.students.krivchansky.multifilemap.MultifileMapShellStateInterface;

public class StorableShellState implements  MultifileMapShellStateInterface<Table, String, Storeable> {
	Table table;
	TableProvider tableProvider;
	
	public StorableShellState(TableProvider provider) {
		tableProvider = provider;
	}
	
	
	public Storeable put(String key, Storeable value) {
		return table.put(key, value);
	}
	
	public Storeable get(String key) {
		return table.get(key);
	}

	public int commit() {
		try {
			return table.commit();
		} catch (IOException e) {
			return -1;
		}
	}

	public int rollback() {
		return table.rollback();
	}

	public int size() {
		return table.size();
	}

	public Storeable remove(String key) {
		return table.remove(key);
	}

	public Table getTable() {
		return table;
	}

	public String keyToString(String key) {
		return key;
	}

	public String valueToString(Storeable value) {
		String string = tableProvider.serialize(table, value);
		return string;
	}

	public String parseKey(String key) {
		return key;
	}

	public Storeable parseValue(String value) {
		try {
            return tableProvider.deserialize(table, value);
        } catch (ParseException e) {
            return null;
        }
	}

	public Table useTable(String name) {
		table = tableProvider.getTable(name);
		return table;
	}

	public Table createTable(String arguments) {
		TableInfo info = null;
        info = LocalUtils.parseCreateCommand(arguments);
        try {
            return tableProvider.createTable(info.getName(), info.getColumnsTypes());
        } catch (IOException e) {
            return null;
        }
	}

	public void dropTable(String name) throws IOException {
		tableProvider.removeTable(name);
		if (table.getName().equals(name)) {
			table = null;
		}
		
	}

	public String getCurrentTableName() {
		return table.getName();
	}

}
