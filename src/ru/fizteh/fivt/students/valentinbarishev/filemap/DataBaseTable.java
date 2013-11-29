package ru.fizteh.fivt.students.valentinbarishev.filemap;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.json.JSONArray;
import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;
import ru.fizteh.fivt.storage.structured.TableProvider;

public final class DataBaseTable implements TableProvider {
    private String tableDir;
    private Map<String, DataBase> tableInUse;

    private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);
    private Lock readLock = readWriteLock.readLock();
    private Lock writeLock = readWriteLock.writeLock();

    public DataBaseTable(String newTableDir) {
        tableDir = newTableDir;
        tableInUse = new HashMap();
    }

    private void checkName(final String name) {
        if ((name == null) || name.trim().length() == 0) {
            throw new IllegalArgumentException("Cannot create table! Wrong name!");
        }

        if (name.matches("[" + '"' + "'\\/:/*/?/</>/|/.\\\\]+") || name.contains(File.separator)
                || name.contains(".")) {
            throw new RuntimeException("Wrong symbols in name!");
        }
    }

    @Override
    public Table createTable(final String tableName, List<Class<?>> columnTypes) throws IOException {
        checkName(tableName);
        String fullPath = tableDir + File.separator + tableName;

        if (columnTypes == null || columnTypes.size() == 0) {
            throw new IllegalArgumentException("wrong type (null)");
        }

        File file = new File(fullPath);

        writeLock.lock();
        try {
        if (file.exists()) {
                return null;
            }

            if (!file.mkdir()) {
                throw new MultiDataBaseException("Cannot create table " + tableName);
            }

            DataBase table = new DataBase(fullPath, this, columnTypes);
            tableInUse.put(tableName, table);
            return table;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void removeTable(final String tableName) throws IOException {
        checkName(tableName);
        String fullPath = tableDir + File.separator + tableName;

        File file = new File(fullPath);
        if (!file.exists()) {
            throw new IllegalStateException("Table not exist already!");
        }
        writeLock.lock();
        try {
            if (!tableInUse.containsKey(tableName)) {
                DataBase base = new DataBase(tableName, this, null);
                base.drop();
            } else {
                tableInUse.get(tableName).drop();
                tableInUse.remove(tableName);
            }
            if (!file.delete()) {
                throw new DataBaseException("Cannot delete a file " + tableName);
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Table getTable(String tableName) {
        checkName(tableName);
        String fullPath = tableDir + File.separator + tableName;

        File file = new File(fullPath);
        if ((!file.exists()) || (file.isFile())) {
            return null;
        }

        readLock.lock();
        try {
            if (tableInUse.containsKey(tableName)) {
                return tableInUse.get(tableName);
            }
        } finally {
            readLock.unlock();
        }

        writeLock.lock();
        try {
            DataBase table = new DataBase(fullPath, this, null);
            tableInUse.put(tableName, table);
            return table;
        } catch (IOException e) {
            throw new DataBaseException(e.getMessage());
        } finally {
            writeLock.unlock();
        }

    }

    @Override
    public Storeable deserialize(Table table, String value) throws ParseException {
        JSONArray json = new JSONArray(value);
        List<Object> values = new ArrayList<>();
        for (int i = 0; i < json.length(); ++i) {
            values.add(json.get(i));
        }

        Storeable storeable;
        try {
            storeable = createFor(table, values);
        } catch (IndexOutOfBoundsException e) {
            throw new ParseException("Invalud number of arguments!", 0);
        } catch (ColumnFormatException e) {
            throw new ParseException(e.getMessage(), 0);
        }

        return storeable;
    }

    @Override
    public String serialize(Table table, Storeable value) throws ColumnFormatException {
        return WorkWithJSON.serialize(table, value);
    }

    @Override
    public Storeable createFor(Table table) {
        return new MyStoreable(table);
    }

    @Override
    public Storeable createFor(Table table, List<?> values) throws ColumnFormatException, IndexOutOfBoundsException {
        return new MyStoreable(table, values);
    }
}

