package ru.fizteh.fivt.students.zhidkovanton.filemap;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

public class State {
    Map<String, String> state = new HashMap<>();

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

    public void print(File input) throws IOException {
        RandomAccessFile in = new RandomAccessFile(input, "rw");

        in.getChannel().truncate(0);
        for (Map.Entry<String, String> curPair : state.entrySet()) {

            in.writeInt(curPair.getKey().getBytes("UTF-8").length);
            in.writeInt(curPair.getValue().getBytes("UTF-8").length);
            in.write(curPair.getKey().getBytes("UTF-8"));
            in.write(curPair.getValue().getBytes("UTF-8"));

        }
        in.close();
    }
}
