package ru.fizteh.fivt.students.kocurba.storeable;

import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;


public class StoreableTable  implements Table {

    private static StoreableState[] state;
    private String tableName = null;
    private List<Class<?>> types;
    private StoreableTableProvider provider;

    public StoreableTable(String tableName, StoreableTableProvider provider, List<Class<?>> types) throws IOException {
        state = new StoreableState[256];
        for (int i = 0; i < 256; ++i) {
            state[i] = new StoreableState(this);
        }

        if (types != null) {
            this.types = types;
            MySignature.setSignature(tableName, this.types);
        } else {
            this.types = MySignature.getSignature(tableName);
        }

        this.provider = provider;
        this.tableName = tableName;
    }

    private void checkName(String name) {
        if ((name == null) || (name.trim().length() == 0)) {
            throw new IllegalArgumentException("Wrong key!");
        }

        for (int i = 0; i < name.length(); ++i) {
            if (Character.isWhitespace(name.charAt(i))) {
                throw new IllegalArgumentException("Wrong key!");
            }
        }
    }

    @Override
    public Storeable get(String key) {
        checkName(key);

        int hashCode = key.hashCode();
        hashCode = Math.abs(hashCode);
        int ndirect = hashCode % 16;
        int nfile = hashCode / 16 % 16;
        String oldValue = state[16 * ndirect + nfile].get(key);
        return JSONCommands.deserialize(this, oldValue);
    }

    @Override
    public int rollback() {
        int ans = 0;
        for (int i = 0; i < 256; ++i) {
            ans += state[i].getNumbersOfChanges();
            state[i].rollback();
        }

        return ans;
    }

    @Override
    public int commit() {
        int ans = 0;
        for (int i = 0; i < 256; ++i) {
            ans += state[i].getNumbersOfChanges();
            state[i].commit();
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

    public int getNumberOfChanges() {
        int ans = 0;
        for (int i = 0; i < 256; ++i) {
            ans += state[i].getNumbersOfChanges();
        }
        return ans;
    }

    @Override
    public Storeable put(String key, Storeable value) {
        checkName(key);

        if (value == null) {
            throw new IllegalArgumentException("You shouldn't put null instead of storeable");
        }

        int hashCode = key.hashCode();
        hashCode = Math.abs(hashCode);
        int ndirect = hashCode % 16;
        int nfile = hashCode / 16 % 16;
        String result = JSONCommands.serialize(this, value);
        String oldValue = state[16 * ndirect + nfile].put(key, result);
        return JSONCommands.deserialize(this, oldValue);

    }

    @Override
    public Storeable remove(String key) {
        checkName(key);

        int hashCode = key.hashCode();
        hashCode = Math.abs(hashCode);
        int ndirect = hashCode % 16;
        int nfile = hashCode / 16 % 16;
        String oldValue = state[16 * ndirect + nfile].remove(key);
        return JSONCommands.deserialize(this, oldValue);
    }

    @Override
    public String getName() {
        File file = new File(tableName);
        return file.getName();
    }

    @Override
    public int getColumnsCount() {
        return types.size();
    }

    @Override
    public Class<?> getColumnType(int columnIndex) {
        if ((columnIndex < 0) || (columnIndex >= types.size())) {
            throw new IndexOutOfBoundsException("Wrong column index: There are only " + types.size()
                    + " but you want " + columnIndex);
        }
        return types.get(columnIndex);
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
                StoreableTableProvider.deleteDirectory(directory);
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
        try {
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

                if (dir.exists() && (dir.listFiles().length == 0 || dir.isFile())) {
                    throw new IOException("Wrong dir");
                }

                if (dir.exists()) {
                    directory += File.separator;
                    for (int k = 0; k < 16; ++k) {
                        String fullPath = directory + Integer.toString(k) + ".dat";
                        File file = new File(fullPath);
                        state[16 * i + k].read(file, i, k);
                    }
                }

            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    public Storeable putStoreable(String key, String value) throws ParseException {
        return put(key, provider.deserialize(this, value));
    }
}
