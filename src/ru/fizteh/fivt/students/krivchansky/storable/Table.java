package src.ru.fizteh.fivt.students.krivchansky.storable;

import java.io.IOException;

public interface Table {
	String getName();
	Storeable get(String a);
	Storeable put(String key, Storeable value) throws ColumnFormatException;
	Storeable remove(String a);
	int size();
	int commit() throws IOException;
	int rollback();
	int getColumnsCount();
	Class<?> getColumnType(int columnIndex) throws IndexOutOfBoundsException;
}