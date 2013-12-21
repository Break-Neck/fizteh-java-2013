package ru.fizteh.fivt.students.krivchansky.filemap;

public interface Table {
	String getName();
	void setAutoCommit(boolean status);
	boolean getAutoCommit();
	int getChangesCounter();
	String get(String a);
	String put(String key, String value);
	String remove(String a);
	int size();
	int commit();
	int rollback();
}
