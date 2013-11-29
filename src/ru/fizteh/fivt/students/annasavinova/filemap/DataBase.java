package ru.fizteh.fivt.students.annasavinova.filemap;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.Set;

import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Table;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.TableProvider;

public class DataBase implements Table {
    protected ThreadLocal<HashMap<String, Storeable>> dataMap = new ThreadLocal<HashMap<String, Storeable>>() {
        @Override
        public HashMap<String, Storeable> initialValue() {
            return new HashMap<>();
        }
    };
    protected HashMap<String, Storeable> commonDataMap = new HashMap<>();
    protected ArrayList<Class<?>> typesList;
    protected DataBaseProvider provider;

    private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);
    private Lock read = readWriteLock.readLock();
    private Lock write = readWriteLock.writeLock();

    private String currTable = "";
    private static String rootDir = "";
    private boolean removed = false;

    public void setRemoved() {
        removed = true;
    }

    public DataBase(String tableName, String root, TableProvider prov) {
        if (prov == null) {
            throw new IllegalArgumentException("Provider is null");
        }
        provider = (DataBaseProvider) prov;
        if (tableName == null) {
            throw new IllegalArgumentException("table name is null");
        } else {
            currTable = tableName;
        }
        if (root == null || root.isEmpty()) {
            throw new IllegalArgumentException("Root name is empty");
        }
        if (!(new File(root).exists())) {
            throw new IllegalStateException("Root not exists");
        }
        if (root.endsWith(File.separator)) {
            rootDir = root;
        } else {
            rootDir = root + File.separatorChar;
        }
        if (!(new File(rootDir + tableName).exists())) {
            throw new IllegalStateException("Table not exists");
        }
    }

    public void setHashMap(HashMap<String, Storeable> map) {
        write.lock();
        try {
            copyMap(commonDataMap, map);
        } finally {
            write.unlock();
        }
    }

    public void setTypes(List<Class<?>> columnTypes) {
        typesList = (ArrayList<Class<?>>) columnTypes;
    }

    protected File getDirWithNum(int dirNum) {
        File res = new File(rootDir + currTable + File.separatorChar + dirNum + ".dir");
        return res;
    }

    protected File getFileWithNum(int fileNum, int dirNum) {
        File res = new File(rootDir + currTable + File.separatorChar + dirNum + ".dir" + File.separatorChar + fileNum
                + ".dat");
        return res;
    }

    protected void unloadData() {
        RandomAccessFile[] filesArray = new RandomAccessFile[256];
        write.lock();
        try {
            for (int i = 0; i < 16; ++i) {
                DataBaseProvider.doDelete(getDirWithNum(i));
            }
            Set<Entry<String, Storeable>> entries = commonDataMap.entrySet();
            for (Map.Entry<String, Storeable> entry : entries) {
                String key = entry.getKey();
                Storeable value = entry.getValue();
                if (value != null) {
                    byte[] keyBytes = key.getBytes("UTF-8");
                    String str = provider.serialize(this, value);
                    byte[] valueBytes = str.getBytes("UTF-8");
                    byte b = 0;
                    b = (byte) Math.abs(keyBytes[0]);
                    int ndirectory = b % 16;
                    int nfile = b / 16 % 16;
                    if (filesArray[ndirectory * 16 + nfile] == null) {
                        File directory = getDirWithNum(ndirectory);
                        if (!directory.exists()) {
                            if (!directory.mkdirs()) {
                                throw new RuntimeException("Cannot unload data correctly: cannot create directory "
                                        + directory.getAbsolutePath());
                            }
                        }
                        File file = getFileWithNum(nfile, ndirectory);
                        if (!file.exists()) {
                            if (!file.createNewFile()) {
                                throw new RuntimeException("Cannot unload data correctly: cannot create file "
                                        + file.getAbsolutePath());
                            }
                        }
                        filesArray[ndirectory * 16 + nfile] = new RandomAccessFile(file, "rw");
                    }
                    filesArray[ndirectory * 16 + nfile].writeInt(keyBytes.length);
                    filesArray[ndirectory * 16 + nfile].writeInt(valueBytes.length);
                    filesArray[ndirectory * 16 + nfile].write(keyBytes);
                    filesArray[ndirectory * 16 + nfile].write(valueBytes);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot unload file correctly", e);
        } finally {
            for (int i = 0; i < 256; ++i) {
                if (filesArray[i] != null) {
                    try {
                        filesArray[i].close();
                    } catch (Throwable e) {
                        // not OK
                    }
                }
            }
            write.unlock();
        }
    }

    private void mergeMaps() {
        write.lock();
        try {
            for (Map.Entry<String, Storeable> entry : dataMap.get().entrySet()) {
                String key = entry.getKey();
                Storeable val = entry.getValue();
                if (val == null) {
                    commonDataMap.remove(key);

                } else {
                    commonDataMap.put(key, val);
                }
            }
        } finally {
            write.unlock();
        }
    }

    private void copyMap(HashMap<String, Storeable> dest, HashMap<String, Storeable> source) {
        dest.clear();
        Set<Map.Entry<String, Storeable>> entries = source.entrySet();
        for (Map.Entry<String, Storeable> entry : entries) {
            write.lock();
            try {
                dest.put(entry.getKey(), entry.getValue());
            } finally {
                write.unlock();
            }
        }
    }

    @Override
    public String getName() {
        if (removed) {
            throw new IllegalStateException("table not exists");
        }
        return currTable;
    }

    private void checkKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key is null");
        }
        if (key.isEmpty() || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Key is empty");
        }
        if (key.split("\\s").length > 1 || key.contains("\t") || key.contains(System.lineSeparator())) {
            throw new IllegalArgumentException("Key contains whitespaces");
        }
    }

    @Override
    public Storeable get(String key) {
        if (removed) {
            throw new IllegalStateException("table not exists");
        }
        checkKey(key);
        Storeable val = null;
        if (dataMap.get().containsKey(key)) {
            val = dataMap.get().get(key);
        } else {
            read.lock();
            try {
                val = commonDataMap.get(key);
            } finally {
                read.unlock();
            }
        }
        return val;
    }

    @Override
    public Storeable put(String key, Storeable value) throws ColumnFormatException {
        if (removed) {
            throw new IllegalStateException("table not exists");
        }
        checkKey(key);
        if (value == null) {
            throw new IllegalArgumentException("Value is null");
        }
        provider.checkColumns(this, value);
        Storeable oldValue = null;
        if (dataMap.get().containsKey(key)) {
            oldValue = dataMap.get().get(key);
        } else {
            read.lock();
            try {
                oldValue = commonDataMap.get(key);
            } finally {
                read.unlock();
            }
        }
        dataMap.get().put(key, value);
        return oldValue;
    }

    @Override
    public Storeable remove(String key) throws IllegalArgumentException {
        if (removed) {
            throw new IllegalStateException("table not exists");
        }
        checkKey(key);
        Storeable val = null;
        if (dataMap.get().containsKey(key)) {
            val = dataMap.get().get(key);
        } else {
            read.lock();
            try {
                val = commonDataMap.get(key);
            } finally {
                read.unlock();
            }
        }
        if (val != null) {
            dataMap.get().put(key, null);
        }
        return val;
    }

    @Override
    public int size() {
        if (removed) {
            throw new IllegalStateException("table not exists");
        }
        int size = 0;
        read.lock();
        try {
            size = commonDataMap.size();
            for (Map.Entry<String, Storeable> entry : dataMap.get().entrySet()) {
                String key = entry.getKey();
                Storeable value = entry.getValue();
                if (value == null && commonDataMap.containsKey(key)) {
                    --size;
                }
                if (value != null && !commonDataMap.containsKey(key)) {
                    ++size;
                }
            }
        } finally {
            read.unlock();
        }
        return size;
    }

    @Override
    public int commit() throws IOException {
        if (removed) {
            throw new IllegalStateException("table not exists");
        }
        int changesCount = countChanges();
        mergeMaps();
        dataMap.get().clear();
        unloadData();
        return changesCount;
    }

    public int countChanges() {
        int count = 0;
        Set<Map.Entry<String, Storeable>> entries = dataMap.get().entrySet();
        read.lock();
        try {
            for (Map.Entry<String, Storeable> entry : entries) {
                String key = entry.getKey();
                Storeable value = entry.getValue();
                Storeable oldValue = null;
                oldValue = commonDataMap.get(key);
                if ((((value == null) || (oldValue == null)) && (value != oldValue))
                        || ((value != null) && (oldValue != null) && !provider.serialize(this, value).equals(
                                provider.serialize(this, oldValue)))) {
                    count++;
                }
            }
        } finally {
            read.unlock();
        }
        return count;
    }

    @Override
    public int rollback() {
        if (removed) {
            throw new IllegalStateException("table not exists");
        }
        int changesCount = countChanges();
        dataMap.get().clear();
        return changesCount;
    }

    @Override
    public int getColumnsCount() {
        if (removed) {
            throw new IllegalStateException("table not exists");
        }
        return typesList.size();
    }

    @Override
    public Class<?> getColumnType(int columnIndex) throws IndexOutOfBoundsException {
        if (removed) {
            throw new IllegalStateException("table not exists");
        }
        if (columnIndex < 0 || columnIndex >= typesList.size()) {
            throw new IndexOutOfBoundsException("Incorrect index " + columnIndex);
        }
        return typesList.get(columnIndex);
    }
}
