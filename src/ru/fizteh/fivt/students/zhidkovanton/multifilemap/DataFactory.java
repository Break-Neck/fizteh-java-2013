package ru.fizteh.fivt.students.zhidkovanton.multifilemap;

import ru.fizteh.fivt.students.zhidkovanton.shell.InvalidCommandException;

import java.io.File;

public class DataFactory {
    private String currentTable = null;
    private String tableDir;

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

    public void removeTable(String name) {
        if (isExists(name)) {
            String fullName = tableDir + File.separator + name;
            deleteDirectory(fullName);
        }
    }

    public String getCurrentName() {
        return currentTable;
    }

    private void checkName(String name) {
        if ((name == null) || name.trim().length() == 0) {
            throw new IllegalArgumentException("Cannot create table! Wrong name!");
        }

        if (name.matches("[" + '"' + "'\\/:/*/?/</>/|/.\\\\]+") || name.contains(File.separator)
                || name.contains(".")) {
            throw new InvalidCommandException("Wrong symbols in name!");
        }
    }

    public DataBase createTable(String name) {
        checkName(name);
        String fullName = tableDir + File.separator + name;

        File file = new File(fullName);

        if (file.exists()) {
            return null;
        }

        if (!file.mkdir()) {
            //throw new InvalidCommandException("Cannot create table " + tableDir);
            System.exit(1);
        }

        return new DataBase();
    }

    public DataBase getTable(String name) {
        checkName(name);
        String fullName = tableDir + File.separator + name;

        File file = new File(fullName);

        if (!file.exists()) {
            return null;
        }
        currentTable = name;

        return new DataBase(name);
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
