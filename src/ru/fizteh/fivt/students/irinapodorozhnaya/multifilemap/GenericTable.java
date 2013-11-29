package ru.fizteh.fivt.students.irinapodorozhnaya.multifilemap;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import ru.fizteh.fivt.students.irinapodorozhnaya.utils.FileStorage;
import ru.fizteh.fivt.students.irinapodorozhnaya.utils.Utils;

public abstract class GenericTable<ValueType> {
    private final String name;
    protected ReadWriteLock lock = new ReentrantReadWriteLock(true);
    protected ReadWriteLock hardDiskLock = new ReentrantReadWriteLock(true);
    protected final File tableDirectory;
    private final Map<String, ValueType> oldDatabase = new HashMap<>();
    private final ThreadLocal<Map<String, ValueType>> changedValues = new ThreadLocal<Map<String, ValueType>>() {
        @Override
        protected Map<String, ValueType> initialValue() {
            return new HashMap<>();
        }
    };

    public GenericTable(String name, File rootDir) {
        tableDirectory = new File(rootDir, name);
        if (!tableDirectory.isDirectory()) {
            throw new IllegalArgumentException(name + "not exist");
        }
        this.name = name;
    }

    public ValueType get(String key) {
        checkKey(key);
        if (changedValues.get().containsKey(key)) {
            return changedValues.get().get(key);
        } else {
            try {
                lock.readLock().lock();
                return  oldDatabase.get(key);
            } finally {
                lock.readLock().unlock();
            }
        }
    }

    public ValueType remove(String key) {
        checkKey(key);
        ValueType res = get(key);
        changedValues.get().put(key, null);
        return res;
    }

    public ValueType put(String key, ValueType value) {
        checkKey(key);
        if (value == null) {
            throw new IllegalArgumentException("null value");
        }
        ValueType res = get(key);
        changedValues.get().put(key, value);
        return res;
    }

    private int countChanges() {
        lock.readLock().lock();
        int res = 0;
        for (String s: changedValues.get().keySet()) {
            if (!checkEquals(changedValues.get().get(s), oldDatabase.get(s))) {
                ++res;
            }
        }
        lock.readLock().unlock();
        return res;
    }

    protected boolean checkEquals(ValueType val1, ValueType val2) {
        if (val1 == null && val2 == null) {
            return true;
        }
        return val1 != null && val2 != null && val1.equals(val2);
    }

    public int commit() throws IOException {
        int res = countChanges();
        try {
            lock.writeLock().lock();
            for (String s: changedValues.get().keySet()) {
                if (changedValues.get().get(s) == null) {
                    oldDatabase.remove(s);
                } else {
                    oldDatabase.put(s, changedValues.get().get(s));
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
        try {
            hardDiskLock.writeLock().lock();
            Map<Integer, Map<String, ValueType>> database = new HashMap<>();
            try {
                lock.readLock().lock();
                for (Map.Entry<String, ValueType> s: oldDatabase.entrySet()) {
                    int nfile = Utils.getNumberOfFile(s.getKey());
                    if (database.get(nfile) == null) {
                        database.put(nfile, new HashMap<String, ValueType>());
                    }
                    database.get(nfile).put(s.getKey(), s.getValue());
                }
            } finally {
                lock.readLock().unlock();
            }

            for (int i = 0; i < 256; ++i) {
                FileStorage.commitDiff(getFile(i), serialize(database.get(i)));
            }

            for (int i = 0; i < 16; ++i) {
                File dir = new File(tableDirectory, i + ".dir");
                dir.delete();
            }
        } finally {
            hardDiskLock.writeLock().unlock();
        }
        changedValues.get().clear();
        return res;
    }


    private File getFile(int nfile) throws IOException {
        File dir = new File(tableDirectory, nfile / 16 + ".dir");
        if (!dir.isDirectory()) {
            if (!dir.mkdir()) {
                throw new IOException("can't create directory");
            }
        }
        File db = new File(dir, nfile % 16 + ".dat");
        if (!db.exists()) {
            if (!db.createNewFile()) {
                throw new IOException("can't create file");
            }
        }
        return db;
    }

    protected abstract Map<String, String> serialize(Map<String, ValueType> values);
    protected abstract Map<String, ValueType> deserialize(Map<String, String> values) throws IOException;

    public int rollback() {
        int res = countChanges();
        changedValues.get().clear();
        return res;
    }

    public int getChangedValuesNumber() {
        return changedValues.get().size();
    }

    public String getName() {
        return name;
    }

    public int size() {
        lock.readLock().lock();
        int res = oldDatabase.size();
        for (Map.Entry<String, ValueType> s: changedValues.get().entrySet()) {
            if (s.getValue() == null && oldDatabase.get(s.getKey()) != null) {
                --res;
            } else if (s.getValue() != null && oldDatabase.get(s.getKey()) == null) {
                ++res;
            }
        }
        lock.readLock().unlock();
        return res;
    }

    public void loadAll() throws IOException {
        loadOldDatabase();
        changedValues.get().clear();
    }

    protected void loadOldDatabase() throws IOException {
        try {
            hardDiskLock.readLock().lock();
            lock.writeLock().lock();
            oldDatabase.clear();
            for (int i = 0; i < 16; ++i) {
                File dir = new File(tableDirectory, i + ".dir");
                if (!dir.isDirectory()) {
                    continue;
                } else if (dir.listFiles().length == 0) {
                    throw new IOException("empty dir");
                }
                for (int j = 0; j < 16; ++j) {
                    File db = new File(dir, j + ".dat");
                    if (db.isFile()) {
                        Map<String, String> fromFile = FileStorage.openDataFile(db, i * 16 + j);
                        oldDatabase.putAll(deserialize(fromFile));
                        if (fromFile.isEmpty()) {
                            throw new IOException("empty file");
                        }
                    }
                }
            }
        } finally {
            lock.writeLock().unlock();
            hardDiskLock.readLock().unlock();
        }
    }

    private void checkKey(String key) throws IllegalArgumentException {
        if (key == null || key.matches("(.*\\s+.*)*")) {
            throw new IllegalArgumentException("key or value null or empty or contain spaces");
        }
    }
}
