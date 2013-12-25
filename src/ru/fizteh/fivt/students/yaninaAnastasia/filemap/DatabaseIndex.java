package ru.fizteh.fivt.students.yaninaAnastasia.filemap;

import ru.fizteh.fivt.storage.structured.Index;
import ru.fizteh.fivt.storage.structured.Storeable;

import java.util.HashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DatabaseIndex implements Index {
    DatabaseTable indexTable;
    int column;
    String indexName;
    HashMap<Object, String> indexes;
    ReadWriteLock lock = new ReentrantReadWriteLock(true);

    DatabaseIndex(DatabaseTable table, int column, String name, HashMap<Object, String> indexes) {
        this.indexTable = table;
        this.column = column;
        this.indexName = name;
        this.indexes = indexes;
    }

    public String getName() {
        return indexName;
    }

    public void updateIndex() {
        lock.writeLock().lock();
        try {
            indexes.clear();
            for (String key : indexTable.oldData.keySet()) {
                Object value = indexTable.oldData.get(key).getColumnAt(column);
                if (value == null) {
                    throw new IllegalStateException("The column contains equal elements");
                }

                if (indexes.containsKey(value)) {
                    throw new IllegalStateException("The column contains equal elements");
                }

                indexes.put(value, key);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Storeable get(String key) {
        if ((key == null) || key.isEmpty()) {
            throw new IllegalArgumentException("The key is illegal");
        }
        Object myKey = null;
        Storeable result = null;
        lock.readLock().lock();
        try {
            if (indexTable.getColumnType(column).equals(String.class)) {
                myKey = key;
            } else {
                myKey = DatabaseTableProvider.typesParser(key, indexTable.getColumnType(column));
            }
            result = indexTable.oldData.get(indexes.get(myKey));
        } finally {
            lock.readLock().unlock();
        }
        return result;
    }
}
