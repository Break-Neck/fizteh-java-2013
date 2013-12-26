package ru.fizteh.fivt.students.baldindima.junit;

import org.json.JSONObject;

import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;

import java.util.ArrayList;
import java.util.List;

public class BaseStoreable implements Storeable {
    private List<Class<?>> types = new ArrayList<>();
    private List<Object> values = new ArrayList<>();

    public BaseStoreable(Table table) {
        for (int i = 0; i < table.getColumnsCount(); ++i) {
            types.add(table.getColumnType(i));
            values.add(null);
            
        }
    }

    public void setValues(List<?> nValues) {
        if (nValues == null) {
            throw new IndexOutOfBoundsException("list of values is null");
        }
        if (nValues.size() != types.size()) {
            throw new IndexOutOfBoundsException("wrong number of values");
        }

        for (int i = 0; i < nValues.size(); ++i) {
        	Object value;
        	if (nValues.get(i).getClass() == JSONObject.NULL.getClass() ||
        			JSONObject.NULL == nValues.get(i)){
        		value = null;
        	} else {
        		value  = parseObject(nValues.get(i).toString(), types.get(i).getSimpleName());
        	}
            
        	
            values.set(i,value);
        }

    }
    
    public static Object parseObject(String string, String expectedClassName) throws ColumnFormatException {
        
    	try {
                switch (expectedClassName) {
                case "Boolean":
                        return Boolean.parseBoolean(string);
                case "Byte":
                        return Byte.parseByte(string);
                case "Float":
                        return Float.parseFloat(string);
                case "Double":
                        return Double.parseDouble(string);
                case "Integer":
                        return Integer.parseInt(string);
                case "Long":
                        return Long.parseLong(string);
                case "String":
                        return string;
                default:
                        throw new ColumnFormatException("wrong type");                                         
                }
        } catch (NumberFormatException catchedException) {
                throw new ColumnFormatException("wrong type");
        }
}

    
    public static boolean isCorrectStoreable(Storeable storeable, Table table) {
        if (storeable == null) {
            throw new IllegalArgumentException("Storeable must be not null");
        }
        if (table == null) {
            throw new IllegalArgumentException("Table must be not null");
        }

        if (!isCorrectSize(storeable, table)) {
            return false;
        }

        for (int i = 0; i < table.getColumnsCount(); ++i) {
            if (storeable.getColumnAt(i) != null
                    && storeable.getColumnAt(i).getClass() != table.getColumnType(i)) {
                return false;
            }
        }

        return true;
    }

    private static boolean isCorrectSize(Storeable storeable, Table table) {
        try {
            storeable.getColumnAt(table.getColumnsCount() - 1);
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
        try {
            storeable.getColumnAt(table.getColumnsCount());
        } catch (IndexOutOfBoundsException e) {
            return true;
        }

        return false;
    }

    void checkIndex(int columnIndex) {
        if (columnIndex < 0 || columnIndex >= types.size()) {
            throw new IndexOutOfBoundsException("wrong index");
        }
    }
   


    public void setColumnAt(int columnIndex, Object value)
            throws ColumnFormatException, IndexOutOfBoundsException {
        checkIndex(columnIndex);
        if (value == null || value == JSONObject.NULL) {
            value = null;
        } else {
        	value  = parseObject(value.toString(), types.get(columnIndex).getSimpleName());
        }
        
        values.set(columnIndex, value);
    }


    public Object getColumnAt(int columnIndex) throws IndexOutOfBoundsException {
        checkIndex(columnIndex);

        return values.get(columnIndex);
    }

    public Integer getIntAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException {
        checkIndex(columnIndex);
        if (!Integer.class.equals(types.get(columnIndex))) {
            throw new ColumnFormatException(columnIndex + " column has incorrect format");
        }
        return (Integer) values.get(columnIndex);
    }


    public Long getLongAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException {
        checkIndex(columnIndex);
        if (!Long.class.equals(types.get(columnIndex))) {
            throw new ColumnFormatException(columnIndex + " column has incorrect format");
        }
        return (long) values.get(columnIndex);
    }

    public Byte getByteAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException {
        checkIndex(columnIndex);
        if (!Byte.class.equals(types.get(columnIndex))) {
            throw new ColumnFormatException(columnIndex + " column has incorrect format");
        }
        return (byte) values.get(columnIndex);
    }

    public Float getFloatAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException {
        checkIndex(columnIndex);
        if (!Float.class.equals(types.get(columnIndex))) {
            throw new ColumnFormatException(columnIndex + " column has incorrect format");
        }
        return (float) values.get(columnIndex);
    }


    public Double getDoubleAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException {
        checkIndex(columnIndex);
        if (!Double.class.equals(types.get(columnIndex))) {
            throw new ColumnFormatException(columnIndex + " column has incorrect format");
        }
        return (double) values.get(columnIndex);
    }


    public Boolean getBooleanAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException {
        checkIndex(columnIndex);
        if (!Boolean.class.equals(types.get(columnIndex))) {
            throw new ColumnFormatException(columnIndex + " column has incorrect format");
        }

        return (boolean) values.get(columnIndex);
    }


    public String getStringAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException {
        checkIndex(columnIndex);
        if (!String.class.equals(types.get(columnIndex))) {
            throw new ColumnFormatException(columnIndex + " column has incorrect format");
        }

        return (String) values.get(columnIndex);
    }
}