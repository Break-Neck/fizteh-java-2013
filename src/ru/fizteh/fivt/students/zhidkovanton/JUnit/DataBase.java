package ru.fizteh.fivt.students.zhidkovanton.JUnit;

import ru.fizteh.fivt.storage.strings.Table;

import java.io.File;
import java.io.IOException;

public class DataBase implements Table {
    private String tableDir;
    private static State[] state;
    private static State[] clone;
    private String tableName = null;

    public DataBase(String tableName) {
        tableDir = System.getProperty("fizteh.db.dir");
        state = new State[256];
        clone = new State[256];
        for (int i = 0; i < 256; ++i) {
            state[i] = new State();
            clone[i] = new State();
        }
        this.tableName = tableName;
    }

    @Override
    public String get(String key) {
        if (key == null || key.trim().equals("")) {
            throw new IllegalArgumentException("Bad key or value!!!");
        }

        try {
            int hashCode = key.hashCode();
            hashCode = Math.abs(hashCode);
            int ndirect = hashCode % 16;
            int nfile = hashCode / 16 % 16;
            String oldValue = state[16 * ndirect + nfile].get(key);
            return oldValue;
        } catch (NullPointerException e) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public int rollback() {
        int ans = getNumberOfChanges();

        for (int i = 0; i < 256; ++i) {
            state[i].clear();
            clone[i].putNewMap(state[i]);
        }

        return ans;
    }

    @Override
    public int commit() {
        int ans = getNumberOfChanges();
        for (int i = 0; i < 256; ++i) {
            clone[i].clear();
            state[i].putNewMap(clone[i]);
        }

        return ans;
    }

    @Override
    public int size() {
        if (state == null) {
            throw new IllegalArgumentException();
        }
        int ans = 0;
        for (int i = 0; i < 256; ++i) {
                ans += state[i].size();
        }
        return ans;
    }

    @Override
    public String put(String key, String value) {
        if (key == null || value == null || key.trim().equals("") || value.trim().equals("")) {
            throw new IllegalArgumentException();
        }

        for (int i = 0; i < key.length(); ++i) {
            if (Character.isWhitespace(key.charAt(i))) {
                throw new IllegalArgumentException("Wrong key!");
            }
        }

        int hashCode = key.hashCode();
        hashCode = Math.abs(hashCode);
        int ndirect = hashCode % 16;
        int nfile = hashCode / 16 % 16;
        String oldValue = state[16 * ndirect + nfile].put(key, value);
        return oldValue;

    }

    @Override
    public String remove(String key) {
        if (key == null || key.trim().equals("")) {
            throw new IllegalArgumentException("Bad key or value!!!");
        }

        int hashCode = key.hashCode();
        hashCode = Math.abs(hashCode);
        int ndirect = hashCode % 16;
        int nfile = hashCode / 16 % 16;
        String oldValue = state[16 * ndirect + nfile].remove(key);
        return oldValue;
    }

    @Override
    public String getName() {
        return tableName;
    }

    public void print() {
        if (tableName == null) {
            return;
        }
        String shablon = tableName + File.separator;

        for (int i = 0; i < 16; ++i) {
            String directory = shablon + Integer.toString(i) + ".dir";

            int flag = 0;

            for (int j = 0; j < 16; ++j) {
                if (!state[16 * i + j].isEmpty()) {
                    flag = 1;
                    break;
                }
            }

            File dir = new File(directory);

            if (dir.exists() && flag == 0) {
                DataFactory.deleteDirectory(directory);
            } else if (dir.exists() || (!dir.exists() && flag == 1)) {
                if (!dir.exists()) {
                    if (!dir.mkdir()) {
                        throw new FileAccessException("Cannot create directory " + directory);
                    }
                }
                directory += File.separator;
                for (int k = 0; k < 16; ++k) {
                    String fullPath = directory + Integer.toString(k) + ".dat";
                    File file = new File(fullPath);

                    if (file.exists() && !state[16 * i + k].isEmpty()) {
                        state[16 * i + k].print(file);
                    }

                    if (file.exists() && state[16 * i + k].isEmpty()) {
                        if (!file.delete()) {
                            throw new FileAccessException("Cannot delete file " + fullPath);
                        }
                    }
                    try {
                        if (!file.exists() && !state[16 * i + k].isEmpty()) {
                            file.createNewFile();
                            state[16 * i + k].print(file);
                        }
                    } catch (IOException e) {
                        throw new FileAccessException(e.getMessage());
                    }
                }
            }

        }
    }

    public int getNumberOfChanges() {
        int ans = 0;
        if (state == null) {
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < 256; ++i) {
            if (clone[i] != null) {
                ans += clone[i].getNumberOfChanges(state[i]);
            }
        }
        return ans;
    }

    public void read() {
        if (tableName == null) {
            throw new IllegalArgumentException("Can't read file because of no file to read");
        }
        for (int i = 0; i < 256; ++i) {
            state[i].clear();
        }

        String shablon = tableName + File.separator;

        for (int i = 0; i < 16; ++i) {
            String directory = shablon + Integer.toString(i) + ".dir";

            File dir = new File(directory);

            if (dir.exists()) {
                directory += File.separator;
                for (int k = 0; k < 16; ++k) {
                    String fullPath = directory + Integer.toString(k) + ".dat";
                    File file = new File(fullPath);
                    state[16 * i + k].read(file);
                    clone[16 * i + k].read(file);
                }
            }

        }
    }
}
