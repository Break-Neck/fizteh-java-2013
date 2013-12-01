package ru.fizteh.fivt.students.belousova.storable;

import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;
import ru.fizteh.fivt.students.belousova.multifilehashmap.AbstractTableProvider;
import ru.fizteh.fivt.students.belousova.utils.StorableUtils;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class StorableTableProvider extends AbstractTableProvider<StorableTable>
        implements ExtendedTableProvider {

    public StorableTableProvider(File directory) throws IOException {
        if (directory == null) {
            throw new IllegalArgumentException("null directory");
        }
        if (!directory.exists()) {
            directory.mkdir();
        } else if (!directory.isDirectory()) {
            throw new IllegalArgumentException("'" + directory.getName() + "' is not a directory");
        }

        dataDirectory = directory;

        if (!directory.canRead()) {
            throw new IOException("directory is unavailable");
        }
        for (File tableFile : directory.listFiles()) {
            tableMap.put(tableFile.getName(), new StorableTable(tableFile, this));
        }
    }

    @Override
    public StorableTable getTable(String name) {
        checkIfClosed();

        if (name == null) {
            throw new IllegalArgumentException("null name");
        }
        if (name.isEmpty()) {
            throw new IllegalArgumentException("empty name");
        }
        if (!name.matches(TABLE_NAME_FORMAT)) {
            throw new IllegalArgumentException("incorrect name");
        }

        tableProviderTransactionLock.writeLock().lock();
        try {
            if (tableMap.get(name) != null) {
                if (tableMap.get(name).isClosed()) {
                    tableMap.remove(name);
                }
            }
        } finally {
            tableProviderTransactionLock.writeLock().unlock();
        }
        return super.getTable(name);
    }

    @Override
    public ExtendedTable createTable(String name, List<Class<?>> columnTypes) throws IOException {
        checkIfClosed();

        if (name == null) {
            throw new IllegalArgumentException("null name");
        }
        if (name.isEmpty()) {
            throw new IllegalArgumentException("empty name");
        }
        if (!name.matches(TABLE_NAME_FORMAT)) {
            throw new IllegalArgumentException("incorrect name");
        }


        if (columnTypes == null) {
            throw new IllegalArgumentException("ColumnTypes list is not set");
        }
        if (columnTypes.isEmpty()) {
            throw new IllegalArgumentException("ColumnTypes list is empty");
        }
        File tableFile = new File(dataDirectory, name);
        tableProviderTransactionLock.writeLock().lock();
        tableProviderTransactionLock.readLock().lock();
        try {
            if (tableMap.containsKey(name)) {
                return null;
            }
            tableFile.mkdir();
            try {
                StorableUtils.writeSignature(tableFile, columnTypes);
            } catch (IOException e) {
                throw new IllegalArgumentException("wrong column type table");
            }
            StorableTable table = new StorableTable(tableFile, this);
            tableMap.put(name, table);
            return table;
        } finally {
            tableProviderTransactionLock.writeLock().unlock();
            tableProviderTransactionLock.readLock().unlock();
        }
    }

    @Override
    public StorableTableLine deserialize(Table table, String value) throws ParseException {
        checkIfClosed();

        try {
            return StorableUtils.readStorableValue(value, table);
        } catch (IndexOutOfBoundsException e) {
            throw new ParseException("wrong data format", 0);
        }
    }

    @Override
    public String serialize(Table table, Storeable value) throws ColumnFormatException {
        checkIfClosed();

        List<Class<?>> columnTypes = new ArrayList<>();
        for (int i = 0; i < table.getColumnsCount(); i++) {
            columnTypes.add(table.getColumnType(i));
        }
        return StorableUtils.writeStorableToString((StorableTableLine) value, columnTypes);
    }

    @Override
    public StorableTableLine createFor(Table table) {
        checkIfClosed();
        if (table == null) {
            throw new IllegalArgumentException("table cannot be null");
        }
        return new StorableTableLine(table);
    }

    @Override
    public StorableTableLine createFor(Table table, List<?> values)
            throws ColumnFormatException, IndexOutOfBoundsException {
        checkIfClosed();
        if (table == null) {
            throw new IllegalArgumentException("table cannot be null");
        }
        if (values == null) {
            throw new IllegalArgumentException("values cannot be null");
        }
        if (values.size() > table.getColumnsCount()) {
            throw new IndexOutOfBoundsException("too many values");
        }

        StorableTableLine storeable = new StorableTableLine(table);
        int columnIndex = 0;
        for (Object value : values) {
            storeable.setColumnAt(columnIndex, value);
            columnIndex++;
        }
        return storeable;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + dataDirectory.getAbsolutePath() + "]";
    }
}
