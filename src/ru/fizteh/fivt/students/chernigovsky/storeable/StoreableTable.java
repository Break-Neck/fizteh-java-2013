package ru.fizteh.fivt.students.chernigovsky.storeable;

import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.students.chernigovsky.junit.AbstractTable;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class StoreableTable extends AbstractTable<Storeable> implements ExtendedStoreableTable, AutoCloseable {
    protected List<Class<?>> columnTypeList;
    protected final ExtendedStoreableTableProvider tableProvider;
    boolean isClosed;

    public StoreableTable(StoreableTable table) {
        super(table);
        isClosed = false;
        tableProvider = table.tableProvider;
        columnTypeList = table.columnTypeList;
    }

    public String getName(){
        if (isClosed) {
            throw new IllegalStateException("table is closed");
        }
        return super.getName();
    }

    public Storeable get(String key) {
        if (isClosed) {
            throw new IllegalStateException("table is closed");
        }
        return super.get(key);
    }

    public void setColumnTypeList(List<Class<?>> newColumnTypeList) {
        columnTypeList = newColumnTypeList;
    }

    public StoreableTable(String name, boolean flag, List<Class<?>> newColumnTypeList, ExtendedStoreableTableProvider newTableProvider) {
        super(name, flag);
        isClosed = false;
        columnTypeList = newColumnTypeList;
        tableProvider = newTableProvider;
    }

    public int getColumnsCount() {
        if (isClosed) {
            throw new IllegalStateException("table is closed");
        }
        return columnTypeList.size();
    }

    /**
     * Возвращает тип значений в колонке.
     *
     * @param columnIndex Индекс колонки. Начинается с нуля.
     * @return Класс, представляющий тип значения.
     *
     * @throws IndexOutOfBoundsException - неверный индекс колонки
     */
    public Class<?> getColumnType(int columnIndex) throws IndexOutOfBoundsException {
        if (isClosed) {
            throw new IllegalStateException("table is closed");
        }
        return columnTypeList.get(columnIndex);
    }

    public Storeable put(String key, Storeable value) {
        if (isClosed) {
            throw new IllegalStateException("table is closed");
        }
        if (value == null) { // maybe need to check: value.trim().isEmpty()
            throw new IllegalArgumentException("value is null");
        }
        if (!StoreableUtils.checkValue(this, value)) {
            throw new ColumnFormatException("invalid value");
        }
        return super.put(key, value);
    }

    public Storeable remove(String key) {
        if (isClosed) {
            throw new IllegalStateException("table is closed");
        }
        return super.remove(key);
    }

    public boolean valuesEqual(Storeable firstValue, Storeable secondValue) {
        return tableProvider.serialize(this, firstValue).equals(tableProvider.serialize(this, secondValue));
    }

    public String toString() {
        if (isClosed) {
            throw new IllegalStateException("table is closed");
        }

        StringBuilder builder = new StringBuilder();

        builder.append(getClass().getSimpleName());
        builder.append("[");
        builder.append(new File(tableProvider.getDbDirectory(), getName()).getAbsolutePath());
        builder.append("]");

        return builder.toString();
    }

    public int size() {
        if (isClosed) {
            throw new IllegalStateException("table is closed");
        }
        return super.size();
    }

    public int commit() throws IOException {
        if (isClosed) {
            throw new IllegalStateException("table is closed");
        }
        int changed = super.commit();

        StoreableUtils.writeTable(this, tableProvider);

        return changed;
    }

    public int rollback() {
        if (isClosed) {
            throw new IllegalStateException("table is closed");
        }
        return super.rollback();
    }

    public synchronized void close() {
        if (isClosed) {
            return;
        }
        rollback();
        isClosed = true;
    }

    public boolean isClosed() {
        return isClosed;
    }

}
