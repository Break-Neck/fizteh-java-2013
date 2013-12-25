package ru.fizteh.fivt.students.irinaGoltsman.multifilehashmap;

import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;
import ru.fizteh.fivt.storage.structured.TableProvider;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DBTable implements Table, AutoCloseable {
    private File tableDirectory;
    private volatile boolean isClosed = false;
    private volatile int size = 0;
    private WeakHashMap<String, Storeable> originalTable = new WeakHashMap<>();
    private List<Class<?>> columnTypes;
    private TableProvider tableProvider;
    private final ReadWriteLock lock = new ReentrantReadWriteLock(true);
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();
    private ThreadLocal<HashMap<String, Storeable>> tableOfChanges
            = new ThreadLocal<HashMap<String, Storeable>>() {
        @Override
        protected HashMap<String, Storeable> initialValue() {
            return new HashMap<>();
        }
    };
    private ThreadLocal<Set<String>> removedKeys = new ThreadLocal<Set<String>>() {
        @Override
        protected HashSet<String> initialValue() {
            return new HashSet<>();
        }
    };

    public DBTable(File inputTableDirectory, TableProvider provider) throws IOException {
        size = FileManager.checkTable(inputTableDirectory);
        tableDirectory = inputTableDirectory;
        tableProvider = provider;
        columnTypes = FileManager.readTableSignature(tableDirectory);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + tableDirectory + "]";
    }

    public void close() {
        if (!isClosed) {
            rollback();
            isClosed = true;
        }
    }

    @Override
    public String getName() {
        checkIsClosed();
        return tableDirectory.getName();
    }

    @Override
    public Storeable get(String key) {
        checkIsClosed();
        if (key == null) {
            throw new IllegalArgumentException("remove: key is null");
        }
        Storeable value = tableOfChanges.get().get(key);
        if (value == null) {
            if (removedKeys.get().contains(key)) {
                return null;
            }
            readLock.lock();
            try {
                value = originalTable.get(key);
            } finally {
                readLock.unlock();
            }
            if (value == null) {
                writeLock.lock();
                try {
                    value = loadRowByKey(key);
                    if (value != null) {
                        originalTable.put(key, value);
                    }
                } finally {
                    writeLock.unlock();
                }
            }
        }
        return value;
    }

    //Проверяет соответствие типов в переданном Storeable с типами таблицы
    private void checkEqualityTypes(Storeable storeable) throws ColumnFormatException {
        for (int numberOfType = 0; numberOfType < columnTypes.size(); numberOfType++) {
            Object type;
            try {
                type = storeable.getColumnAt(numberOfType);
            } catch (IndexOutOfBoundsException e) {
                throw new ColumnFormatException("table put: types of storeable mismatch");
            }
            if (type != null) {
                if (!columnTypes.get(numberOfType).equals(type.getClass())) {
                    throw new ColumnFormatException("table put: types of storeable mismatch");
                }
            }
        }
        try {  //Проверка на то, что число колонок в storeable не больше допустимого
            storeable.getColumnAt(columnTypes.size());
        } catch (IndexOutOfBoundsException e) {
            return;
        }
        throw new ColumnFormatException("storeable has more columns then must have");
    }

    @Override
    public Storeable put(String key, Storeable value) throws ColumnFormatException {
        checkIsClosed();
        if (value == null || key == null) {
            throw new IllegalArgumentException("put: key or value is null");
        }
        if (key.trim().isEmpty()) {
            throw new IllegalArgumentException("put: key is empty");
        }
        if (key.matches(".*\\s+.*")) {
            throw new IllegalArgumentException("put: key contains white space");
        }
        Storeable newValue = value;
        checkEqualityTypes(newValue);
        Storeable originalValue = null;
        readLock.lock();
        try {
            originalValue = originalTable.get(key);
        } finally {
            readLock.unlock();
        }
        if (originalValue == null) {
            writeLock.lock();
            try {
                originalValue = loadRowByKey(key);
                if (originalValue != null) {
                    originalTable.put(key, originalValue);
                }
            } finally {
                writeLock.unlock();
            }
        }
        Storeable oldValue = tableOfChanges.get().put(key, newValue);
        //Значит здесь впервые происходит перезаписывание старого значения.
        if (!removedKeys.get().contains(key) && oldValue == null) {
            oldValue = originalValue;
        }
        if (originalValue != null) {
            removedKeys.get().add(key);
        }
        return oldValue;
    }

    @Override
    public Storeable remove(String key) {
        checkIsClosed();
        if (key == null) {
            throw new IllegalArgumentException("table remove: key is null");
        }
        Storeable originalValue;
        readLock.lock();
        try {
            originalValue = originalTable.get(key);
        } finally {
            readLock.unlock();
        }
        if (originalValue == null) {
            writeLock.lock();
            try {
                originalValue = loadRowByKey(key);
                if (originalValue != null) {
                    originalTable.put(key, originalValue);
                }
            } finally {
                writeLock.unlock();
            }
        }
        Storeable value = tableOfChanges.get().get(key);
        if (value == null) {
            if (!removedKeys.get().contains(key)) {
                if (originalValue != null) {
                    removedKeys.get().add(key);
                    value = originalValue;
                }
            }
        } else {
            tableOfChanges.get().remove(key);
            if (originalValue != null) {
                removedKeys.get().add(key);
            }
        }
        return value;
    }

    @Override
    public int size() {
        checkIsClosed();
        writeLock.lock();
        try {
            Set<String> keys = removedKeys.get();
            for (String key : keys) {
                if (!removedKeys.get().contains(key)) {
                    continue;
                }
                Storeable originalValue = loadRowByKey(key);
                if (originalValue == null) {
                    removedKeys.get().remove(key);
                    continue;
                }
                Storeable value = tableOfChanges.get().get(key);
                if (value != null) {
                    if (checkStoreableForEquality(value, originalValue)) {
                        tableOfChanges.get().remove(key);
                        removedKeys.get().remove(key);
                    }
                }
            }
            keys = tableOfChanges.get().keySet();
            for (String key : keys) {
                if (!tableOfChanges.get().containsKey(key)) {
                    continue;
                }
                Storeable originalValue = loadRowByKey(key);
                Storeable value = tableOfChanges.get().get(key);
                if (originalValue != null && value != null
                        && checkStoreableForEquality(value, originalValue)) {
                    tableOfChanges.get().remove(key);
                }
            }
        } finally {
            writeLock.unlock();
        }
        return size - removedKeys.get().size() + tableOfChanges.get().size();
    }

    //@return Количество сохранённых ключей.
    @Override
    public int commit() throws IOException {
        checkIsClosed();
        List<String> keys = new ArrayList<>(tableOfChanges.get().keySet());
        List<Storeable> values = new ArrayList<>(tableOfChanges.get().values());
        HashMap<String, String> serializedTableOfChanges = new HashMap<>();
        for (int i = 0; i < values.size(); i++) {
            String serializedValue = tableProvider.serialize(this, values.get(i));
            serializedTableOfChanges.put(keys.get(i), serializedValue);
        }
        int count;
        writeLock.lock();
        try {
            originalTable.clear();
            count = FileManager.writeTableOnDisk(tableDirectory, serializedTableOfChanges,
                    removedKeys.get());
            size = FileManager.readSize(tableDirectory);
        } finally {
            writeLock.unlock();
        }
        tableOfChanges.get().clear();
        removedKeys.get().clear();
        return count;
    }

    @Override
    public int rollback() {
        checkIsClosed();
        int count = countTheNumberOfChanges();
        tableOfChanges.get().clear();
        removedKeys.get().clear();
        return count;
    }

    @Override
    public int getColumnsCount() {
        checkIsClosed();
        return columnTypes.size();
    }

    @Override
    public Class<?> getColumnType(int columnIndex) throws IndexOutOfBoundsException {
        checkIsClosed();
        if (columnIndex >= columnTypes.size() || columnIndex < 0) {
            throw new IndexOutOfBoundsException("invalid column index: " + columnIndex);
        }
        return columnTypes.get(columnIndex);
    }

    public int countTheNumberOfChanges() {
        checkIsClosed();
        int countOfChanges = tableOfChanges.get().size();
        writeLock.lock();
        try {
            for (String key : removedKeys.get()) {
                Storeable originalValue = loadRowByKey(key);
                if (originalValue == null) {
                    removedKeys.get().remove(key);
                    continue;
                }
                Storeable value = tableOfChanges.get().get(key);
                if (value != null) {
                    if (checkStoreableForEquality(value, originalValue)) {
                        tableOfChanges.get().remove(key);
                        removedKeys.get().remove(key);
                        countOfChanges--;
                    }
                } else {
                    countOfChanges++;
                }
            }
            for (String key : tableOfChanges.get().keySet()) {
                Storeable originalValue = loadRowByKey(key);
                Storeable value = tableOfChanges.get().get(key);
                if (originalValue != null && value != null
                        && checkStoreableForEquality(value, originalValue)) {
                    tableOfChanges.get().remove(key);
                    countOfChanges--;
                }
            }
        } finally {
            writeLock.unlock();
        }
        return countOfChanges;
    }

    public boolean checkStoreableForEquality(Storeable first, Storeable second) {
        String string1 = tableProvider.serialize(this, first);
        String string2 = tableProvider.serialize(this, second);
        return string1.equals(string2);
    }

    private void checkIsClosed() throws IllegalStateException {
        if (isClosed) {
            throw new IllegalStateException("table was closed");
        }
    }

    public Boolean isClosed() {
        return isClosed;
    }

    private Storeable loadRowByKey(String key) {
        String value;
        try {
            value = FileManager.loadValueByKey(key, tableDirectory);
        } catch (IOException e) {
            throw new RuntimeException("error while reading a dir: " + e.getMessage(), e);
        }
        if (value == null) {
            return null;
        }
        Storeable rowValue;
        try {
            rowValue = tableProvider.deserialize(this, value);
        } catch (ParseException e) {
            throw new RuntimeException("error while parsing storeable: " + e.getMessage(), e);
        }
        return rowValue;
    }
}
