package ru.fizteh.fivt.students.yaninaAnastasia.filemap;

import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DatabaseTable implements Table, AutoCloseable {
    public HashMap<String, Storeable> oldData;
    public ThreadLocal<TransactionWithModifies> transaction;
    private String tableName;
    public List<Class<?>> columnTypes;
    DatabaseTableProvider provider;
    private ReadWriteLock transactionLock = new ReentrantReadWriteLock(true);
    volatile boolean isClosed;


    public DatabaseTable(String name, List<Class<?>> colTypes, DatabaseTableProvider providerRef) {
        isClosed = false;
        this.tableName = name;
        oldData = new HashMap<String, Storeable>();
        transaction = new ThreadLocal<TransactionWithModifies>() {
            @Override
            public TransactionWithModifies initialValue() {
                return new TransactionWithModifies();
            }
        };
        columnTypes = colTypes;
        provider = providerRef;
        for (final Class<?> columnType : columnTypes) {
            if (columnType == null || ColumnTypes.fromTypeToName(columnType) == null) {
                throw new IllegalArgumentException("unknown column type");
            }
        }
    }

    public DatabaseTable(DatabaseTable other) {
        this.tableName = other.tableName;
        this.columnTypes = other.columnTypes;
        this.provider = other.provider;
        this.oldData = other.oldData;
        isClosed = false;
        this.transaction = other.transaction;
    }

    public static int getDirectoryNum(String key) {
        int keyByte = Math.abs(key.getBytes(StandardCharsets.UTF_8)[0]);
        return keyByte % 16;
    }

    public static int getFileNum(String key) {
        int keyByte = Math.abs(key.getBytes(StandardCharsets.UTF_8)[0]);
        return (keyByte / 16) % 16;
    }

    public String getName() {
        isCloseChecker();
        if (tableName == null) {
            throw new IllegalArgumentException("Table name cannot be null");
        }
        return tableName;
    }

    public Storeable get(String key) throws IllegalArgumentException {
        isCloseChecker();
        if (key == null || (key.isEmpty() || key.trim().isEmpty())) {
            throw new IllegalArgumentException("Table name cannot be null");
        }

        if (transaction.get().modifiedData.containsKey(key)) {
            return transaction.get().modifiedData.get(key);
        }
        if (transaction.get().deletedKeys.contains(key)) {
            return null;
        }
        transactionLock.readLock().lock();
        try {
            return oldData.get(key);
        } finally {
            transactionLock.readLock().unlock();
        }
    }

    public Storeable put(String key, Storeable value) throws IllegalArgumentException {
        isCloseChecker();
        if ((key == null) || (key.trim().isEmpty())) {
            throw new IllegalArgumentException("Key can not be null");
        }
        if (key.matches("\\s*") || key.split("\\s+").length != 1) {
            throw new IllegalArgumentException("Key contains whitespaces");
        }
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        checkAlienStoreable(value);
        for (int index = 0; index < getColumnsCount(); ++index) {
            switch (ColumnTypes.fromTypeToName(columnTypes.get(index))) {
                case "String":
                    String stringValue = (String) value.getColumnAt(index);
                    if (stringValue == null) {
                        continue;
                    }
                    break;
                default:
            }
        }
        Storeable oldValue = null;
        oldValue = transaction.get().modifiedData.get(key);
        if (oldValue == null && !transaction.get().deletedKeys.contains(key)) {
            transactionLock.readLock().lock();
            try {
                oldValue = oldData.get(key);
            } finally {
                transactionLock.readLock().unlock();
            }
        }
        transaction.get().modifiedData.put(key, value);
        if (transaction.get().deletedKeys.contains(key)) {
            transaction.get().deletedKeys.remove(key);
        }
        transaction.get().uncommittedChanges = changesCount();
        return oldValue;
    }

    public Storeable remove(String key) throws IllegalArgumentException {
        isCloseChecker();
        if (key == null || (key.isEmpty() || key.trim().isEmpty())) {
            throw new IllegalArgumentException("Key name cannot be null");
        }
        Storeable oldValue = null;
        oldValue = transaction.get().modifiedData.get(key);
        if (oldValue == null && !transaction.get().deletedKeys.contains(key)) {
            transactionLock.readLock().lock();
            try {
                oldValue = oldData.get(key);
            } finally {
                transactionLock.readLock().unlock();
            }
        }
        if (transaction.get().modifiedData.containsKey(key)) {
            transaction.get().modifiedData.remove(key);
            transactionLock.readLock().lock();
            try {
                if (oldData.containsKey(key)) {
                    transaction.get().deletedKeys.add(key);
                }
            } finally {
                transactionLock.readLock().unlock();
            }
        } else {
            transaction.get().deletedKeys.add(key);
        }
        transaction.get().uncommittedChanges = changesCount();
        return oldValue;
    }

    public int size() {
        isCloseChecker();
        transactionLock.readLock().lock();
        try {
            return oldData.size() + diffSize();
        } finally {
            transactionLock.readLock().unlock();
        }
    }

    public int commit() {
        isCloseChecker();
        int recordsCommitted = 0;
        transactionLock.writeLock().lock();
        try {
            recordsCommitted = Math.abs(changesCount());
            for (String keyToDelete : transaction.get().deletedKeys) {
                oldData.remove(keyToDelete);
            }
            for (String keyToAdd : transaction.get().modifiedData.keySet()) {
                if (transaction.get().modifiedData.get(keyToAdd) != null) {
                    oldData.put(keyToAdd, transaction.get().modifiedData.get(keyToAdd));
                }
            }
            transaction.get().deletedKeys.clear();
            transaction.get().modifiedData.clear();
            TableBuilder tableBuilder = new TableBuilder(provider, this);
            save(tableBuilder);
            transaction.get().uncommittedChanges = 0;
        } finally {
            transactionLock.writeLock().unlock();
        }
        return recordsCommitted;
    }

    public int rollback() {
        isCloseChecker();
        int recordsDeleted = Math.abs(changesCount());

        transaction.get().deletedKeys.clear();
        transaction.get().modifiedData.clear();

        transaction.get().uncommittedChanges = 0;

        return recordsDeleted;
    }

    public Class<?> getColumnType(int columnIndex) throws IndexOutOfBoundsException {
        isCloseChecker();
        if (columnIndex < 0 || columnIndex >= getColumnsCount()) {
            throw new IndexOutOfBoundsException("wrong index");
        }
        return columnTypes.get(columnIndex);
    }

    public Storeable storeableGet(String key) {
        transactionLock.readLock().lock();
        try {
            return oldData.get(key);
        } finally {
            transactionLock.readLock().unlock();
        }
    }

    public void storeablePut(String key, Storeable value) {
        transactionLock.writeLock().lock();
        try {
            oldData.put(key, value);
        } finally {
            transactionLock.writeLock().unlock();
        }
    }

    public boolean save(TableBuilder tableBuilder) {
        transactionLock.readLock().lock();
        try {
            if (oldData == null) {
                return true;
            }
        } finally {
            transactionLock.readLock().unlock();
        }
        if (tableName.equals("")) {
            return true;
        }
        File tablePath = new File(provider.getDatabaseDirectory(), tableName);
        for (int i = 0; i < 16; i++) {
            String directoryName = String.format("%d.dir", i);
            File path = new File(tablePath, directoryName);
            boolean isDirEmpty = true;
            ArrayList<HashSet<String>> keys = new ArrayList<HashSet<String>>(16);
            for (int j = 0; j < 16; j++) {
                keys.add(new HashSet<String>());
            }
            transactionLock.readLock().lock();
            try {
                for (String step : oldData.keySet()) {
                    int nDirectory = getDirectoryNum(step);
                    if (nDirectory == i) {
                        int nFile = getFileNum(step);
                        keys.get(nFile).add(step);
                        isDirEmpty = false;
                    }
                }
            } finally {
                transactionLock.readLock().unlock();
            }

            if (isDirEmpty) {
                try {
                    if (path.exists()) {
                        DatabaseTableProvider.recRemove(path);
                    }
                } catch (IOException e) {
                    return false;
                }
                continue;
            }
            if (path.exists()) {
                File file = path;
                try {
                    if (!DatabaseTableProvider.recRemove(file)) {
                        System.err.println("File was not deleted");
                        return false;
                    }
                } catch (IOException e) {
                    return false;
                }
            }
            if (!path.mkdir()) {
                return false;
            }
            for (int j = 0; j < 16; j++) {
                File filePath = new File(path, String.format("%d.dat", j));
                try {
                    saveTable(keys.get(j), filePath.toString(), tableBuilder);
                } catch (IOException e) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean saveTable(Set<String> keys, String path, TableBuilder tableBuilder) throws IOException {
        if (keys.isEmpty()) {
            try {
                Files.delete(Paths.get(path));
            } catch (IOException e) {
                return false;
            }
            return false;
        }
        try (RandomAccessFile temp = new RandomAccessFile(path, "rw")) {
            long offset = 0;
            temp.setLength(0);
            for (String step : keys) {
                offset += step.getBytes(StandardCharsets.UTF_8).length + 5;
            }
            for (String step : keys) {
                byte[] bytesToWrite = step.getBytes(StandardCharsets.UTF_8);
                temp.write(bytesToWrite);
                temp.writeByte(0);
                temp.writeInt((int) offset);
                String myOffset = tableBuilder.get(step);
                offset += myOffset.getBytes(StandardCharsets.UTF_8).length;
            }
            for (String key : keys) {
                String value = tableBuilder.get(key);
                temp.write(value.getBytes(StandardCharsets.UTF_8));
            }
            temp.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private int changesCount() {
        HashSet<String> tempSet = new HashSet<>();
        HashSet<String> toRemove = new HashSet<>();
        tempSet.addAll(transaction.get().modifiedData.keySet());
        tempSet.addAll(transaction.get().deletedKeys);
        transactionLock.readLock().lock();
        try {
            for (String key : tempSet) {
                if (tempSet.contains(key) && compare(oldData.get(key), transaction.get().modifiedData.get(key))) {
                    toRemove.add(key);
                }
            }
        } finally {
            transactionLock.readLock().unlock();
        }
        return tempSet.size() - toRemove.size();
    }

    private int diffSize() {
        int result = 0;
        for (final String key : transaction.get().modifiedData.keySet()) {
            Storeable oldValue;
            transactionLock.readLock().lock();
            try {
                oldValue = oldData.get(key);
            } finally {
                transactionLock.readLock().unlock();
            }
            Storeable newValue = transaction.get().modifiedData.get(key);
            if (oldValue == null && newValue != null) {
                result += 1;
            }
        }
        for (final String key : transaction.get().deletedKeys) {
            transactionLock.readLock().lock();
            try {
                if (oldData.containsKey(key)) {
                    result -= 1;
                }
            } finally {
                transactionLock.readLock().unlock();
            }
        }
        return result;
    }

    public static boolean compare(Storeable key1, Storeable key2) {
        if (key1 == null && key2 == null) {
            return true;
        }
        if (key1 == null || key2 == null) {
            return false;
        }
        return key1.equals(key2);
    }

    public int getColumnsCount() {
        isCloseChecker();
        return columnTypes.size();
    }

    public void checkAlienStoreable(Storeable storeable) {
        for (int index = 0; index < getColumnsCount(); ++index) {
            try {
                Object o = storeable.getColumnAt(index);
                if (o == null) {
                    continue;
                }
                if (!o.getClass().equals(getColumnType(index))) {
                    throw new ColumnFormatException("Alien storeable with incompatible types");
                }
            } catch (IndexOutOfBoundsException e) {
                throw new ColumnFormatException("Alien storeable with less columns");
            }
        }
        try {
            storeable.getColumnAt(getColumnsCount());
        } catch (IndexOutOfBoundsException e) {
            return;
        }
        throw new ColumnFormatException("Alien storeable with more columns");
    }

    public void isCloseChecker() {
        if (isClosed) {
            throw new IllegalStateException("It is closed");
        }
    }

    @Override
    public String toString() {
        isCloseChecker();
        return String.format("%s[%s]", getClass().getSimpleName(), new File(provider.curDir, tableName).toString());
    }

    @Override
    public void close() throws Exception {
        if (isClosed) {
            return;
        }
        rollback();
        isClosed = true;
    }

    public void defineTransaction(TransactionWithModifies transaction) {
        this.transaction.set(transaction);
    }

    public TransactionWithModifies getTransaction() {
        return this.transaction.get();
    }
}
