package ru.fizteh.fivt.students.zhidkovanton.parallel;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

import java.util.concurrent.locks.Lock;

public class State {
    public Map<String, String> state;


    private ThreadLocal<HashMap<String, String>> diff = new ThreadLocal<HashMap<String, String>>() {
        @Override
        public HashMap<String, String> initialValue() {
            return new HashMap<String, String>();
        }
    };

    private ThreadLocal<HashSet<String>> deleted = new ThreadLocal<HashSet<String>>() {
        @Override
        public HashSet<String> initialValue() {
            return new HashSet<String>();
        }
    };
    private Lock readLock;
    private Lock writeLock;
    public State(DataBase table) {
        state = new HashMap<>();
        readLock = table.readLock;
        writeLock = table.writeLock;
    }

    public String put(String key, String value) {
        String result = null;

        if (diff.get().containsKey(key)) {
            result = diff.get().get(key);
        } else {
            readLock.lock();
            try {
                if (state.containsKey(key)) {
                    result = state.get(key);
                }
            } finally {
                readLock.unlock();
            }
        }

        if (deleted.get().contains(key)) {
            deleted.get().remove(key);
            result = null;
        }

        diff.get().put(key, value);

        return result;
    }

    public String get(String key) {
        if (deleted.get().contains(key)) {
            return null;
        }

        if (diff.get().containsKey(key)) {
            return diff.get().get(key);
        }

        readLock.lock();
        try {
            if (state.containsKey(key)) {
                return state.get(key);
            }
        } finally {
            readLock.unlock();
        }

        return null;
    }

    public String remove(String key) {
        if (deleted.get().contains(key)) {
            return null;
        }
        String result = null;

        if (diff.get().containsKey(key)) {
            result = diff.get().get(key);
            diff.get().remove(key);
            deleted.get().add(key);
            return result;
        }

        readLock.lock();
        try {
            if (state.containsKey(key)) {
                result = state.get(key);
                deleted.get().add(key);
            }
        } finally {
            readLock.unlock();
        }

        return result;
    }

    public void commit() {
        deletedSame();

        if (diff.get().size() == 0 && deleted.get().size() == 0) {
            return;
        }

        for (Map.Entry<String, String> node : diff.get().entrySet()) {
            state.put(node.getKey(), node.getValue());
        }

        for (String key : deleted.get()) {
            state.remove(key);
        }


        diff.get().clear();
        deleted.get().clear();
    }

    public void rollback() {
        diff.get().clear();
        deleted.get().clear();
    }

    public void print(File input) {
        try {
            RandomAccessFile in = new RandomAccessFile(input, "rw");

            in.getChannel().truncate(0);
            for (Map.Entry<String, String> curPair : state.entrySet()) {

                in.writeInt(curPair.getKey().getBytes("UTF-8").length);
                in.writeInt(curPair.getValue().getBytes("UTF-8").length);
                in.write(curPair.getKey().getBytes("UTF-8"));
                in.write(curPair.getValue().getBytes("UTF-8"));

            }
            in.close();
        } catch (IOException e) {
            throw new FileAccessException(e.getMessage());
        }
    }

    public int size() {
        readLock.lock();
        try {
            deletedSame();
            int result = diff.get().size() + state.size() - deleted.get().size();
            for (String key : diff.get().keySet()) {
                if (state.containsKey(key)) {
                    --result;
                }
            }
            return result;
        } finally {
            readLock.unlock();
        }
    }

    void deletedSame() {
        Set<String> newDeleted = new HashSet<>();
        newDeleted.addAll(deleted.get());

        for (String key : state.keySet()) {
            if (state.get(key).equals(diff.get().get(key))) {
                diff.get().remove(key);
            }
            if (newDeleted.contains(key)) {
                newDeleted.remove(key);
            }
        }

        for (String key : deleted.get()) {
            if (diff.get().containsKey(key)) {
                diff.get().remove(key);
            }
        }

        for (String key : newDeleted) {
            deleted.get().remove(key);
        }
    }

    public int getNumbersOfChanges() {
        readLock.lock();
        try {
            deletedSame();
            return diff.get().size() + deleted.get().size();
        } finally {
            readLock.unlock();
        }
    }

    public void read(File input, int ndir, int nfile) {
        try {
            if (input.exists() && input.length() == 0) {
                throw new IOException("Empty File");
            }
            if (input.exists()) {
                RandomAccessFile in = new RandomAccessFile(input, "rw");

                while (in.getFilePointer() < in.length() - 1) {
                    int keyLength = in.readInt();
                    int valueLength = in.readInt();
                    if ((keyLength <= 0) || (valueLength <= 0)) {
                        in.close();
                        throw new IllegalArgumentException("wrong format");
                    }

                    byte[] key;
                    byte[] value;

                    try {
                        key = new byte[keyLength];
                        value = new byte[valueLength];
                    } catch (OutOfMemoryError e) {
                        in.close();
                        throw new IllegalArgumentException("too large key or value");
                    }
                    in.read(key);
                    in.read(value);
                    String keyString = new String(key, "UTF-8");
                    String valueString = new String(value, "UTF-8");
                    int hashCode = keyString.hashCode();
                    hashCode = Math.abs(hashCode);
                    int q = hashCode % 16;
                    int qq = hashCode / 16 % 16;
                    if (q != ndir || qq != nfile) {
                        throw new IOException("wrong key placement");
                    }
                    state.put(keyString, valueString);
                }
                in.close();
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public void clear() {
        state.clear();
    }

    public boolean isEmpty() {
        return state.isEmpty();
    }
}
