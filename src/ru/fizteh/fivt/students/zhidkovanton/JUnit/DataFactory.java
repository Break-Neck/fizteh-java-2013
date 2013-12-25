package ru.fizteh.fivt.students.zhidkovanton.JUnit;

import ru.fizteh.fivt.storage.strings.TableProvider;
import ru.fizteh.fivt.storage.strings.Table;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DataFactory implements TableProvider {
    private String currentTable = null;
    private String tableDir;
    private Map<String, DataBase> allTables = new HashMap<>();


    public DataFactory(String directory) {
        tableDir = directory;
    }

    public static void deleteDirectory(String dir) {
        File file = new File(dir);
        File[] files = file.listFiles();
        for (int i = 0; i < files.length; ++i) {
            if (files[i].isFile()) {
                if (!files[i].delete()) {
                    throw new FileAccessException("Cant delete file " + file.toString());
                }
            } else {
                deleteDirectory(files[i].toString());
            }
        }
        if (!file.delete()) {
            throw new FileAccessException("Cant delete directory " + file.toString());
        }
    }

    @Override
    public void removeTable(String name) {
        checkName(name);
        if (isExists(name)) {
            String fullName = tableDir + File.separator + name;
            deleteDirectory(fullName);
        } else {
            throw new IllegalStateException();
        }

    }

    public String getCurrentName() {
        return currentTable;
    }

    private void checkName(String name) {
        if ((name == null) || name.trim().length() == 0) {
            throw new IllegalArgumentException();
        }

        if (name.matches("[" + '"' + "'\\/:/*/?/</>/|/.\\\\]+") || name.contains(File.separator)
                || name.contains(".")) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Table createTable(String name) {
        checkName(name);
        String fullName = tableDir + File.separator + name;

        File file = new File(fullName);

        if (file.exists()) {
            if (allTables.get(name) == null) {
                allTables.put(name, new DataBase(name));
            }
            return null;
        }

        if (!file.mkdir()) {
            throw new IllegalArgumentException();
        }

        allTables.put(name, new DataBase(name));
        return allTables.get(name);
    }

    @Override
    public Table getTable(String name) {
        checkName(name);
        String fullName = tableDir + File.separator + name;

        File file = new File(fullName);

        if (!file.exists()) {
            return null;
        }
        currentTable = name;

        return allTables.get(name);
    }

    public boolean isExists(String name) {
        checkName(name);
        String fullName = tableDir + File.separator + name;

        File file = new File(fullName);

        if (file.exists()) {
            return true;
        }
        return false;
    }
}
