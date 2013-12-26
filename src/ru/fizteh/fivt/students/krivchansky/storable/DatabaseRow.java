package ru.fizteh.fivt.students.krivchansky.storable;


import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;

public class DatabaseRow implements Storeable {
	final List<Class<?>> classes = new ArrayList<>();
	List<Object> columns = new ArrayList<>();
	
	public DatabaseRow(List<Class<?>> classes) {
		this.classes.addAll(classes);
		for (int i = 0; i < classes.size(); ++i) {
			columns.add(null);
		}
	}
	
	public DatabaseRow() {}
	
	private void checkBounds(int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= classes.size()) {
            throw new IndexOutOfBoundsException(String.format("index out of bound: %d", index));
        }
    }
	
	private void checkColumnType(int columnIndex, Class<?> actualType) throws ColumnFormatException {
        if (!actualType.isAssignableFrom(classes.get(columnIndex))) {
            throw new ColumnFormatException(String.format("incorrect type: expected type: %s actual type: %s",
                    classes.get(columnIndex).getName(), actualType.getName()));
        }
    }
	
	public boolean equals(Object obj) {
        DatabaseRow otherStoreable = (DatabaseRow) obj;
        return otherStoreable.columns.equals(columns) && otherStoreable.classes.equals(classes);
    }

    public String getStringAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException {
        checkBounds(columnIndex);
        checkColumnType(columnIndex, String.class);
        return (String) columns.get(columnIndex);
    }
    
    public Object getColumnAt(int columnIndex) throws IndexOutOfBoundsException {
        checkBounds(columnIndex);
        return columns.get(columnIndex);
    }

    public Integer getIntAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException {
        checkBounds(columnIndex);
        checkColumnType(columnIndex, Integer.class);
        return (Integer) columns.get(columnIndex);
    }

    public Long getLongAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException {
        checkBounds(columnIndex);
        checkColumnType(columnIndex, Long.class);
        return (Long) columns.get(columnIndex);
    }

    public Byte getByteAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException {
        checkBounds(columnIndex);
        checkColumnType(columnIndex, Byte.class);
        return (Byte) columns.get(columnIndex);
    }

    public Float getFloatAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException {
        checkBounds(columnIndex);
        checkColumnType(columnIndex, Float.class);
        return (Float) columns.get(columnIndex);
    }

    public Double getDoubleAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException {
        checkBounds(columnIndex);
        checkColumnType(columnIndex, Double.class);
        return (Double) columns.get(columnIndex);
    }

    public Boolean getBooleanAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException {
        checkBounds(columnIndex);
        checkColumnType(columnIndex, Boolean.class);
        return (Boolean) columns.get(columnIndex);
    }
	
    @Override
	public String toString() {
        return LocalUtils.join(columns);
    }
	
	public void setColumns(List<?> values) throws ColumnFormatException, IndexOutOfBoundsException {
        if (values.size() != classes.size()) {
            throw new IndexOutOfBoundsException();
        }

        columns.clear();

        for (int index = 0; index < values.size(); ++index) {
            columns.add(values.get(index));
        }
    }
	
	public void setColumnAt(int columnIndex, Object value) throws ColumnFormatException, IndexOutOfBoundsException {
        checkBounds(columnIndex);
        if (value != null) {
            checkColumnType(columnIndex, value.getClass());
            try {
            	LocalUtils.checkValue(value, value.getClass());
            } catch(ParseException e) {
            	throw new IllegalArgumentException("incorrect value: " + value.toString());
            }
        }
        columns.set(columnIndex, value);
    }
	
	public void addColumn(Class<?> columnType) {
        classes.add(columnType);
        columns.add(null);
    }
	
	
}
