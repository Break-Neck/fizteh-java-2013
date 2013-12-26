package ru.fizteh.fivt.students.krivchansky.filemap;



public class FileMapShellState implements FileMapShellStateInterface<MyTable, String, String> {

	public MyTable table = null;
	
	public String put (String key, String value) {
		if (key == null || value == null)
			throw new IllegalArgumentException("wrong value or key");
		if (key.trim().isEmpty() || value.trim().isEmpty())
			throw new IllegalArgumentException("wrong value or key");
		return table.put(key, value);
	}
	
	public String get (String key) {
		return table.get(key);
	}
	
	public int commit() {
		return table.commit();
	}
	
	public int rollback() {
		return table.rollback();
	}
	
	public int size() {
		return table.size();
	}
	
	public String remove(String key) {
		return table.remove(key);
	}
	
	public MyTable getTable() {
		return table;
	}
	
	public String keyToString(String key) {
		return key;
	}
	
	public String valueToString(String value) {
		return value;
	}
	
	public String parseKey(String key) {
		return key;
	}
	
	public String parseValue(String value) {
		return value;
	}
}