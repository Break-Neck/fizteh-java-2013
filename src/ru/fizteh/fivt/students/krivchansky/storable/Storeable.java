package ru.fizteh.fivt.students.krivchansky.storable;

public interface Storeable {
	void setColumnAt(int columnIndex, Object value) throws ColumnFormatException, IndexOutOfBoundsException;
	
	Object getColumnAt(int columnIndex) throws IndexOutOfBoundsException;
	
	Integer getIntAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException;
	
	Long getLongAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException;
	
	Byte getByteAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException;
	
	Float getFloatAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException;
	
	Double getDoubleAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException;
	
	Boolean getBooleanAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException;
	
	String getStringAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException;

}
