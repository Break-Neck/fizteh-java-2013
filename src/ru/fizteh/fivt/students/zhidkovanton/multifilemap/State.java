package ru.fizteh.fivt.students.zhidkovanton.multifilemap;

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

    public void clear() {
        state.clear();
    }

    public void read(File input) {
        try {
            if (input.exists()) {
                RandomAccessFile in = new RandomAccessFile(input, "rw");

                while (in.getFilePointer() < in.length() - 1) {
                    int keyLength = in.readInt();
                    int valueLength = in.readInt();
                    if ((keyLength <= 0) || (valueLength <= 0)) {
                        in.close();
                        throw new IOException("wrong format");
                    }

                    byte[] key;
                    byte[] value;

                    try {
                        key = new byte[keyLength];
                        value = new byte[valueLength];
                    } catch (OutOfMemoryError e) {
                        in.close();
                        throw new IOException("too large key or value");
                    }
                    in.read(key);
                    in.read(value);
                    String keyString = new String(key, "UTF-8");
                    String valueString = new String(value, "UTF-8");
                    state.put(keyString, valueString);
                }
                in.close();
            }
        } catch (IOException e) {
            throw new FileAccessException(e.getMessage());
        }
    }

    public boolean isEmpty() {
        return state.isEmpty();
    }
}
