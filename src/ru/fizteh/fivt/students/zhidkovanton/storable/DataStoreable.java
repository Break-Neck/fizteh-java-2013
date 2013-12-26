package ru.fizteh.fivt.students.zhidkovanton.storable;

import org.json.JSONObject;
import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;

import java.util.ArrayList;
import java.util.List;

public class DataStoreable implements Storeable {
    private List<Class<?>> types = new ArrayList<>();
    private List<Object> values = new ArrayList<>();

    public DataStoreable(Table table) {
        for (int i = 0; i < table.getColumnsCount(); ++i) {
            types.add(table.getColumnType(i));
            values.add(null);
        }
    }

    public DataStoreable(Table table, List<?> values) {
        if (values == null) {
            throw new IndexOutOfBoundsException("list of values is null!!! check it!!!");
        }

        if (values.size() != table.getColumnsCount()) {
            throw new IndexOutOfBoundsException("Hmm... Number of columns in table differs from it in the list");
        }

        for (int i = 0; i < table.getColumnsCount(); ++i) {
            Object value;
            types.add(table.getColumnType(i));
            if (values.get(i).getClass() == JSONObject.NULL.getClass()
                    || JSONObject.NULL == values.get(i)) {
                value = null;
            } else {
                value = getType(values.get(i).toString(), types.get(i).getSimpleName());
            }
            this.values.add(value);
        }
    }

    private static Object getType(String value, String expectedType) throws ColumnFormatException {
        try {
            switch (expectedType) {
                case "Boolean":
                    return Boolean.parseBoolean(value);
                case "Byte":
                    return Byte.parseByte(value);
                case "Float":
                    return Float.parseFloat(value);
                case "Double":
                    return Double.parseDouble(value);
                case "Integer":
                    return Integer.parseInt(value);
                case "Long":
                    return Long.parseLong(value);
                case "String":
                    return value;
                default:
                    throw new ColumnFormatException("wrong type (expect " + expectedType + " but found " + value + ")");
            }
        } catch (NumberFormatException e) {
            throw new ColumnFormatException("wrong type (" + value + "isn't number)");
        }
    }

    void checkBounds(int index) {
        if (index < 0 || index >= types.size()) {
            throw new IndexOutOfBoundsException("index out of bounds!");
        }
    }

    void checkType(int index, Class<?> value) {
        if ((value != null) && (value != types.get(index))) {
            throw new ColumnFormatException("wrong type");
        }
    }

    public int getSize() {
        return types.size();
    }

    @Override
    public void setColumnAt(int columnIndex, Object value) throws ColumnFormatException, IndexOutOfBoundsException {
        checkBounds(columnIndex);
        if (value == null || value == JSONObject.NULL) {
            value = null;
        } else {
            value = getType(value.toString(), types.get(columnIndex).getSimpleName());
        }

        values.set(columnIndex, value);
    }

    @Override
    public Object getColumnAt(int columnIndex) throws IndexOutOfBoundsException {
        checkBounds(columnIndex);
        return values.get(columnIndex);
    }

    @Override
    public Integer getIntAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException {
        checkBounds(columnIndex);
        checkType(columnIndex, Integer.class);
        return (Integer) values.get(columnIndex);
    }

    @Override
    public Long getLongAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException {
        checkBounds(columnIndex);
        checkType(columnIndex, Long.class);
        return (long) values.get(columnIndex);
    }

    @Override
    public Byte getByteAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException {
        checkBounds(columnIndex);
        checkType(columnIndex, Byte.class);
        return (byte) values.get(columnIndex);
    }

    @Override
    public Float getFloatAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException {
        checkBounds(columnIndex);
        checkType(columnIndex, Float.class);
        return (float) values.get(columnIndex);
    }

    @Override
    public Double getDoubleAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException {
        checkBounds(columnIndex);
        checkType(columnIndex, Double.class);
        return (double) values.get(columnIndex);
    }

    @Override
    public Boolean getBooleanAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException {
        checkBounds(columnIndex);
        checkType(columnIndex, Boolean.class);
        return (boolean) values.get(columnIndex);
    }

    @Override
    public String getStringAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException {
        checkBounds(columnIndex);
        checkType(columnIndex, String.class);
        return (String) values.get(columnIndex);
    }


}
