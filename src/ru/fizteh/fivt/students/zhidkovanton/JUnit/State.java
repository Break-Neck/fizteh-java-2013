package ru.fizteh.fivt.students.zhidkovanton.JUnit;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


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
            int size = 0;

            ArrayList<String> keys = new ArrayList<String>();

            for (String key : state.keySet()) {
                 keys.add(key);
            }

            for (int i = 0; i < keys.size(); ++i) {
                size += keys.get(i).getBytes(StandardCharsets.UTF_8).length + 5;
            }

            for (int i = 0; i < keys.size(); ++i) {

                in.write(keys.get(i).getBytes("UTF-8"));
                in.write('\0');
                in.writeInt(size);
                size += keys.get(i).getBytes(StandardCharsets.UTF_8).length;

            }

            for (int i = 0; i < keys.size(); ++i) {

                in.write(state.get(keys.get(i)).getBytes("UTF-8"));

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
            if (!newState.get(curPair.getKey()).equals(curPair.getValue())) {
                ++ans;
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

    public void read(File input) {
        try {
            if (input.exists()) {
                RandomAccessFile in = new RandomAccessFile(input, "rw");

                ArrayList<Integer> offsets = new ArrayList<Integer>();
                ArrayList<String> keys = new ArrayList<String>();

                do {
                    ArrayList<Byte> myKey = new ArrayList<Byte>();
                    byte b = in.readByte();

                    while (b != 0) {
                        myKey.add(b);
                        b = in.readByte();
                    }
                    if (myKey.size() == 0) {
                        throw new FileAccessException("Empty key");
                    }

                    byte[] keyInBytes = new byte[myKey.size()];
                    for (int i = 0; i < keyInBytes.length; ++i) {
                        keyInBytes[i] = myKey.get(i);
                    }

                    String key = new String(keyInBytes, "UTF-8");

                    keys.add(key);

                    int offset = in.readInt();
                    if (offset < 0) {
                        throw new FileAccessException("Bad offset");
                    }
                    offsets.add(offset);
                } while (in.getFilePointer() != offsets.get(0));


                for (int i = 0; i < keys.size(); ++i) {
                    byte[] bytes = new byte[offsets.get(i + 1) - offsets.get(i)];
                    in.read(bytes);
                    state.put(keys.get(i), new String(bytes, "UTF-8"));
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
