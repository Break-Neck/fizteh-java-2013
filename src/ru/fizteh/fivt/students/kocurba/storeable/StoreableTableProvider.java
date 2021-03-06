package ru.fizteh.fivt.students.kocurba.storeable;

import org.json.JSONArray;
import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;
import ru.fizteh.fivt.storage.structured.TableProvider;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class StoreableTableProvider implements TableProvider {

    private String currentTable = null;
    private String tableDir;
    private Map<String, StoreableTable> allTables = new HashMap<>();
    private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);
    private Lock readLock = readWriteLock.readLock();
    private Lock writeLock = readWriteLock.writeLock();


    public StoreableTableProvider(String directory) {
        tableDir = directory;
    }

    public static void deleteDirectory(String dir) {
        File file = new File(dir);
        File[] files = file.listFiles();
        for (int i = 0; i < files.length; ++i) {
            if (files[i].isFile()) {
                if (!files[i].delete()) {
                    throw new FileAccessException("Cant delete file " + file.toString());
                }
            } else {
                deleteDirectory(files[i].toString());
            }
        }
        if (!file.delete()) {
            throw new FileAccessException("Cant delete directory " + file.toString());
        }
    }

    @Override
    public void removeTable(String name) throws IOException {
        checkName(name);
        if (isExists(name)) {
            writeLock.lock();
            try {
                String fullName = tableDir + File.separator + name;
                deleteDirectory(fullName);
            } finally {
                writeLock.unlock();
            }
        } else {
            throw new IllegalStateException(name + " not exists");
        }

    }

    public String getCurrentName() {
        return currentTable;
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
    public Table createTable(String name, List<Class<?>> columnTypes) throws IOException {
        checkName(name);
        String fullName = tableDir + File.separator + name;

        if (columnTypes == null || columnTypes.size() == 0) {
            throw new IllegalArgumentException("wrong type (null)");
        }

        File file = new File(fullName);



        writeLock.lock();
        try {
            if (file.exists()) {
                return null;
            }

            if (!file.mkdir()) {
                throw new IllegalArgumentException("Can't create a table");
            }

            allTables.put(name, new StoreableTable(fullName, this, columnTypes));
            return allTables.get(name);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Table getTable(String name) {
        try {
            checkName(name);
            String fullName = tableDir + File.separator + name;

            File file = new File(fullName);

            if (!file.exists()) {
                return null;
            }

            if (file.isFile() || file.listFiles().length == 0) {
                throw new IOException("Wrong dir");
            }
            currentTable = name;

            if (allTables.get(name) == null) {
                allTables.put(name, new StoreableTable(fullName, this, null));
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

        StoreableTable dataBase = allTables.get(name);

        readLock.lock();
        try {
            dataBase.read();
        } finally {
            readLock.unlock();
        }

        return dataBase;
    }

    public boolean isExists(String name) {
        checkName(name);
        String fullName = tableDir + File.separator + name;

        File file = new File(fullName);

        if (file.exists()) {
            return true;
        }
        return false;
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
            throw new ParseException("Invalid number of arguments!", 0);
        } catch (ColumnFormatException e) {
            throw new ParseException(e.getMessage(), 0);
        }

        return storeable;
    }

    @Override
    public String serialize(Table table, Storeable value) throws ColumnFormatException {
        return JSONCommands.serialize(table, value);
    }

    @Override
    public Storeable createFor(Table table) {
        return new FileStoreable(table);
    }

    @Override
    public Storeable createFor(Table table, List<?> values) throws ColumnFormatException, IndexOutOfBoundsException {
        return new FileStoreable(table, values);
    }
}
