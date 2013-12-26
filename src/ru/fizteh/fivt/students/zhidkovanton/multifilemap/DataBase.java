package ru.fizteh.fivt.students.zhidkovanton.multifilemap;

import ru.fizteh.fivt.students.zhidkovanton.shell.InvalidCommandException;

import java.io.File;
import java.io.IOException;

public class DataBase {
    private String tableDir;
    private static State[] state;
    private String tableName = null;

    public DataBase() {
    }

    public DataBase(String tableName) {
        tableDir = System.getProperty("fizteh.db.dir");
        state = new State[256];
        for (int i = 0; i < 256; ++i) {
            state[i] = new State();
        }
        this.tableName = tableName;
    }

    public String get(String key) {
        if (key == null) {
            throw new InvalidCommandException("Bad key or value!!!");
        }

        int hashCode = key.hashCode();
        hashCode = Math.abs(hashCode);
        int ndirect = hashCode % 16;
        int nfile = hashCode / 16 % 16;
        String oldValue = state[16 * ndirect + nfile].get(key);
        return oldValue;
    }

    public String put(String key, String value) {
        if (key == null || value == null) {
            throw new InvalidCommandException("Bad key or value!!!");
        }
        int hashCode = key.hashCode();
        hashCode = Math.abs(hashCode);
        int ndirect = hashCode % 16;
        int nfile = hashCode / 16 % 16;
        String oldValue = state[16 * ndirect + nfile].put(key, value);
        boolean a = state[0].isEmpty();
        return oldValue;
    }

    public String remove(String key) {
        if (key == null) {
            throw new InvalidCommandException("Bad key or value!!!");
        }

        int hashCode = key.hashCode();
        hashCode = Math.abs(hashCode);
        int ndirect = hashCode % 16;
        int nfile = hashCode / 16 % 16;
        String oldValue = state[16 * ndirect + nfile].remove(key);
        return oldValue;
    }

    public String getName() {
        return tableName;
    }

    public void print() {
        if (tableName == null) {
            return;
        }
        String shablon = tableDir + File.separator + tableName + File.separator;

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

    public void read() {
        if (tableName == null) {
            throw new FileAccessException("Can't read file because of no file to read");
        }
        for (int i = 0; i < 256; ++i) {
            state[i].clear();
        }

        String shablon = tableDir + File.separator + tableName + File.separator;

        for (int i = 0; i < 16; ++i) {
            String directory = shablon + Integer.toString(i) + ".dir";

            File dir = new File(directory);

            if (dir.exists()) {
                directory += File.separator;
                for (int k = 0; k < 16; ++k) {
                    String fullPath = directory + Integer.toString(k) + ".dat";
                    File file = new File(fullPath);
                    state[16 * i + k].read(file);
                }
            }

        }
    }
}
