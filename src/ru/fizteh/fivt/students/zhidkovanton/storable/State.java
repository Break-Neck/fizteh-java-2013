package ru.fizteh.fivt.students.zhidkovanton.storable;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

public class State {
    public Map<String, String> state;

    public State() {
        state = new HashMap<>();
    }

    public String put(String key, String value) {
        String oldValue = state.get(key);
        state.put(key, value);
        return oldValue;
    }

    public String get(String key) {
        return state.get(key);
    }

    public String remove(String key) {
        String oldValue = state.get(key);
        if (oldValue != null) {
            state.remove(key);
        }
        return oldValue;
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
        return state.size();
    }

    public void clear() {
        state.clear();
    }

    public int getNumberOfChanges(State newState) {
        int ans = 0;
        if (state.isEmpty()) {
            return newState.size();
        }
        if (newState.isEmpty()) {
            return state.size();
        }
        for (Map.Entry<String, String> curPair : state.entrySet()) {
            if (newState.get(curPair.getKey()) != null) {
                if (!newState.get(curPair.getKey()).equals(curPair.getValue())) {
                    ++ans;
                }
            }
        }

        for (Map.Entry<String, String> curPair : state.entrySet()) {
            if (newState.get(curPair.getKey()) == null && curPair.getValue() != null) {
                ++ans;
            }
        }
        return ans;
    }

    public void putNewMap(State newState) {
        for (Map.Entry<String, String> curPair : state.entrySet()) {
            newState.put(curPair.getKey(), curPair.getValue());
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

    public boolean isEmpty() {
        return state.isEmpty();
    }
}
