package src.ru.fizteh.fivt.students.krivchansky.filemap;

public abstract class TableUsingStrings extends SomeStorage<String, String> implements Table {
	protected TableUsingStrings(String dir, String name) {
		super(dir, name);
	}
	
	public String remove(String key) {
		return removeFromStorage(key);
	}
	
	public int size() {
		return sizeOfStorage();
	}
	
	public String get(String key) {
		return getFromStorage(key);
	}
	
	public String put(String key, String value) {
		return putIntoStorage(key, value);
	}
	
	public int rollback() {
		return rollbackStorage();
	}
	
	public int commit() {
		return commitStorage();
	}
	
}
