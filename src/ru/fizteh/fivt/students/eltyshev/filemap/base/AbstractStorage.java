package ru.fizteh.fivt.students.eltyshev.filemap.base;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractStorage<Key, Value> {
    class TransactionChanges {
        HashMap<Key, Value> modifiedData;
        int size;
        int uncommittedChanges;

        TransactionChanges() {
            this.modifiedData = new HashMap<Key, Value>();
            this.size = 0;
            this.uncommittedChanges = 0;
        }

        public void addChange(Key key, Value value) {
            modifiedData.put(key, value);
        }

        public int applyChanges() {
            int recordsChanged = 0;
            for (final Key key : modifiedData.keySet()) {
                Value newValue = modifiedData.get(key);
                if (!FileMapUtils.compareKeys(oldData.get(key), newValue)) {
                    if (newValue == null) {
                        oldData.remove(key);
                    } else {
                        oldData.put(key, (Value) newValue);
                    }
                    recordsChanged += 1;
                }
            }
            return recordsChanged;
        }

        public int countChanges() {
            int recordsChanged = 0;
            for (final Key key : modifiedData.keySet()) {
                Value newValue = modifiedData.get(key);
                if (!FileMapUtils.compareKeys(oldData.get(key), newValue)) {
                    recordsChanged += 1;
                }
            }
            return recordsChanged;
        }

        public int calcSize() {
            int result = 0;
            for (final Key key : modifiedData.keySet()) {
                Value newValue = modifiedData.get(key);
                Value oldValue = oldData.get(key);
                if (newValue == null && oldValue != null) {
                    result -= 1;
                }
                if (newValue != null && oldValue == null) {
                    result += 1;
                }
            }
            return result;
        }

        public Value getValue(Key key) {
            if (modifiedData.containsKey(key)) {
                return modifiedData.get(key);
            }
            return oldData.get(key);
        }

        public int getSize() {
            return oldData.size() + calcSize();
        }

        public void increaseUncommittedChanges() {
            uncommittedChanges += 1;
        }

        public int getUncommittedChanges() {
            return uncommittedChanges;
        }

        public void clear() {
            modifiedData.clear();
            size = 0;
            uncommittedChanges = 0;
        }
    }

    private final Lock transactionLock = new ReentrantLock(true);

    public static final Charset CHARSET = StandardCharsets.UTF_8;
    // Data
    protected final HashMap<Key, Value> oldData;
    protected final ThreadLocal<TransactionChanges> transactionChanges = new ThreadLocal<TransactionChanges>() {
        @Override
        public TransactionChanges initialValue() {
            return new TransactionChanges();
        }
    };

    final private String tableName;
    private String directory;

    // Strategy
    protected abstract void load() throws IOException;

    protected abstract void save() throws IOException;

    // Constructor
    public AbstractStorage(String directory, String tableName) {
        this.directory = directory;
        this.tableName = tableName;
        oldData = new HashMap<Key, Value>();
        try {
            load();
        } catch (IOException e) {
            throw new IllegalArgumentException("invalid file format");
        }
    }

    public int getUncommittedChangesCount() {
        return transactionChanges.get().getUncommittedChanges();
    }

    // Table implementation
    public String getName() {
        return tableName;
    }

    public Value storageGet(Key key) throws IllegalArgumentException {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null!");
        }
        return transactionChanges.get().getValue(key);
    }

    public Value storagePut(Key key, Value value) throws IllegalArgumentException {
        if (key == null || value == null) {
            String message = key == null ? "key " : "value ";
            throw new IllegalArgumentException(message + "cannot be null");
        }

        Value oldValue = transactionChanges.get().getValue(key);

        transactionChanges.get().addChange(key, value);
        return oldValue;
    }

    public Value storageRemove(Key key) throws IllegalArgumentException {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        if (storageGet(key) == null) {
            return null;
        }

        Value oldValue = transactionChanges.get().getValue(key);
        transactionChanges.get().addChange(key, null);
        transactionChanges.get().increaseUncommittedChanges();
        return oldValue;
    }

    public int storageSize() {
        return transactionChanges.get().getSize();
    }

    public int storageCommit() {
        try {
            transactionLock.lock();
            int recordsCommitted = transactionChanges.get().applyChanges();
            transactionChanges.get().clear();

            try {
                save();
            } catch (IOException e) {
                System.err.println("storageCommit: " + e.getMessage());
                return 0;
            }

            return recordsCommitted;
        } finally {
            transactionLock.unlock();
        }
    }

    public int storageRollback() {
        int recordsDeleted = transactionChanges.get().countChanges();
        transactionChanges.get().clear();
        return recordsDeleted;
    }

    public String getDirectory() {
        return directory;
    }

    void rawPut(Key key, Value value) {
        oldData.put(key, value);
    }

    Value rawGet(Key key) {
        return oldData.get(key);
    }
}


