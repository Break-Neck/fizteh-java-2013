package ru.fizteh.fivt.students.sterzhanovVladislav.fileMap;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;
import ru.fizteh.fivt.storage.structured.TableProvider;
import ru.fizteh.fivt.students.sterzhanovVladislav.fileMap.storeable.StoreableRow;
import ru.fizteh.fivt.students.sterzhanovVladislav.fileMap.storeable.StoreableUtils;
import ru.fizteh.fivt.students.sterzhanovVladislav.shell.ShellUtility;

public class FileMapProvider implements TableProvider {

    public static final String SIGNATURE_FILE_NAME = "signature.tsv";

    private Path rootDir;
    private HashMap<String, FileMap> tables;

    @Override
    public FileMap getTable(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException();
        }
        Path dbPath = Paths.get(rootDir.normalize() + "/" + name);
        if (dbPath == null || !isValidFileName(name)) {
            throw new IllegalArgumentException("Invalid path");
        }
        try {
            FileMap table = tables.get(name);
            if (table == null) {
                table = IOUtility.parseDatabase(dbPath);
                table.setProvider(this);
                tables.put(name, table);
            }
            return table; 
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public FileMap createTable(String name, List<Class<?>> columnTypes) throws IOException {
        if (name == null || name.isEmpty() || columnTypes == null || columnTypes.isEmpty()) {
            throw new IllegalArgumentException();
        }
        Path dbPath = Paths.get(rootDir.normalize() + "/" + name);
        if (dbPath == null || !isValidFileName(name)) {
            throw new IllegalArgumentException("Invalid path");
        }
        if (dbPath.toFile().exists()) {
            return null;
        }
        createFileStructure(dbPath, columnTypes);
        FileMap newFileMap = new FileMap(name, columnTypes);
        newFileMap.setProvider(this);
        tables.put(name, newFileMap);
        return newFileMap;
    }

    @Override
    public void removeTable(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException();
        }
        Path dbPath = Paths.get(rootDir.normalize() + "/" + name);
        if (dbPath == null || !isValidFileName(name)) {
            throw new IllegalArgumentException("Invalid path");
        }
        try {
            ShellUtility.removeDir(dbPath);
            tables.remove(name);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }
    
    public void removeAllTables() {
        for (String tableName : tables.keySet()) {
            removeTable(tableName);
        }
    }
    
    public String getRootDir() {
        return rootDir.toString();
    }

    public FileMapProvider(String root) throws IllegalArgumentException, IOException {
        if (root == null || root.isEmpty()) {
            throw new IllegalArgumentException();
        }
        rootDir = Paths.get(root);
        if (rootDir == null) {
            throw new IllegalArgumentException("Error: RootDir can not be null");
        }
        if (!rootDir.toFile().exists()) {
            if (!rootDir.toFile().mkdir()) {
                throw new IOException("Error: Root directory does not exist, unable to create root directory");
            }
        }
        if (!rootDir.toFile().isDirectory()) {
            throw new IllegalArgumentException("Error: Root did not resolve to a valid directory");
        }
        tables = new HashMap<String, FileMap>();
    }
    
    private static void createFileStructure(Path dbPath, List<Class<?>> columnTypes) throws IOException {
        if (!dbPath.toFile().mkdir()) {
            throw new IOException("Unable to create directory");
        }
        IOUtility.writeSignature(dbPath, columnTypes);
    }
    
    private static boolean isValidFileName(String name) {
        return !(name.contains("\\") || name.contains("/")
                || name.contains(":") || name.contains("*")
                || name.contains("?") || name.contains("\"")
                || name.contains("<") || name.contains(">")
                || name.contains("\n") || name.contains(" ")
                || name.contains("|") || name.contains("\t"));
    }

    @Override
    public Storeable deserialize(Table table, String value)
            throws ParseException {
        return StoreableUtils.deserialize(value, generateSignature(table));
    }

    @Override
    public String serialize(Table table, Storeable value)
            throws ColumnFormatException {
        if (!StoreableUtils.validate(value, generateSignature(table))) {
            throw new ColumnFormatException("wrong type (can't serialize value according to signature)");
        }
        return StoreableUtils.serialize(value, generateSignature(table));
    }

    @Override
    public Storeable createFor(Table table) {
        return new StoreableRow(generateSignature(table));
    }

    @Override
    public Storeable createFor(Table table, List<?> values)
            throws ColumnFormatException, IndexOutOfBoundsException {
        return new StoreableRow(generateSignature(table), values);
    }
    
    private static List<Class<?>> generateSignature(Table table) {
        List<Class<?>> classList = new ArrayList<Class<?>>();
        for (int classID = 0; classID < table.getColumnsCount(); ++classID) {
            classList.add(table.getColumnType(classID));
        }
        return classList;
    }
}
