package ru.fizteh.fivt.students.musin.filemap;

import org.json.JSONArray;
import org.json.JSONException;
import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;
import ru.fizteh.fivt.storage.structured.TableProvider;
import ru.fizteh.fivt.students.musin.shell.FileSystemRoutine;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FileMapProvider implements TableProvider {
    File location;
    HashMap<String, MultiFileMap> used;

    public FileMapProvider(File location) {
        if (location == null) {
            throw new IllegalArgumentException("Null location");
        }
        this.location = location;
        used = new HashMap<>();
    }

    private boolean badSymbolCheck(String string) {
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) <= 31) {
                return false;
            }
            if (string.charAt(i) == '\\') {
                return false;
            }
            if (string.charAt(i) == '/') {
                return false;
            }
            if (string.charAt(i) == '*') {
                return false;
            }
            if (string.charAt(i) == ':') {
                return false;
            }
            if (string.charAt(i) == '<') {
                return false;
            }
            if (string.charAt(i) == '>') {
                return false;
            }
            if (string.charAt(i) == '"') {
                return false;
            }
            if (string.charAt(i) == '|') {
                return false;
            }
            if (string.charAt(i) == '?') {
                return false;
            }
        }
        return true;
    }

    public boolean isValidLocation() {
        if (!location.exists() || location.exists() && !location.isDirectory()) {
            return false;
        }
        return true;
    }

    public boolean isValidContent() {
        if (!isValidLocation()) {
            return false;
        }
        for (File f : location.listFiles()) {
            if (!f.isDirectory()) {
                return false;
            }
        }
        return true;
    }

    public MultiFileMap getTable(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Null name");
        }
        if (name.equals("")) {
            throw new IllegalArgumentException("Empty name");
        }
        if (!badSymbolCheck(name)) {
            throw new RuntimeException("Illegal characters");
        }
        if (!isValidLocation()) {
            throw new RuntimeException("Database location is invalid");
        }
        File dir = new File(location, name);
        if (!dir.exists()) {
            return null;
        }
        if (dir.exists() && !dir.isDirectory()) {
            throw new RuntimeException(String.format("%s is not a directory", name));
        }
        MultiFileMap newMap = used.get(name);
        if (newMap != null) {
            return newMap;
        } else {
            newMap = new MultiFileMap(dir, 16, this);
            try {
                newMap.loadFromDisk();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            return newMap;
        }
    }

    public MultiFileMap createTable(String name, List<Class<?>> columnTypes) throws IOException {
        if (name == null) {
            throw new IllegalArgumentException("Null name");
        }
        if (columnTypes == null) {
            throw new IllegalArgumentException("Null columnTypes");
        }
        if (columnTypes.size() == 0) {
            throw new ColumnFormatException("Can't create table with no columns");
        }
        for (Class<?> columnType : columnTypes) {
            boolean check = false;
            if (columnType == null) {
                throw new IllegalArgumentException("Null doesn't specify a type");
            }
            for (Class<?> allowedClass : FixedList.CLASSES) {
                if (columnType == allowedClass) {
                    check = true;
                    break;
                }
            }
            if (!check) {
                throw new IllegalArgumentException(String.format("wrong type %s not supported", columnType.toString()));
            }
        }
        if (name.equals("")) {
            throw new IllegalArgumentException("Empty name");
        }
        if (!badSymbolCheck(name)) {
            throw new RuntimeException("Illegal characters");
        }
        if (!isValidLocation()) {
            throw new RuntimeException("Database location is invalid");
        }
        File dir = new File(location, name);
        if (dir.exists() && !dir.isDirectory()) {
            throw new RuntimeException(String.format("%s is not a directory", name));
        }
        if (dir.exists()) {
            return null;
        }
        if (!dir.mkdir()) {
            throw new RuntimeException("Can't create directory for the table");
        }
        MultiFileMap result = new MultiFileMap(dir, 16, this, columnTypes);
        result.writeToDisk();
        used.put(name, result);
        return result;
    }

    public void removeTable(String name) throws IOException {
        if (name == null) {
            throw new IllegalArgumentException("Null name");
        }
        if (name.equals("")) {
            throw new IllegalArgumentException("Empty name");
        }
        if (!badSymbolCheck(name)) {
            throw new RuntimeException("Illegal characters");
        }
        if (!isValidLocation()) {
            throw new RuntimeException("Database location is invalid");
        }
        File dir = new File(location, name);
        if (dir.exists() && !dir.isDirectory()) {
            throw new RuntimeException(String.format("%s is not a directory", name));
        }
        if (!dir.exists()) {
            throw new IllegalStateException("Table doesn't exist");
        }
        if (!FileSystemRoutine.deleteDirectoryOrFile(dir)) {
            throw new RuntimeException("Unable to delete some files");
        }
        used.remove(name);
    }

    public FixedList deserialize(Table table, String value) throws ParseException {
        if (value == null) {
            throw new IllegalArgumentException("Null string as argument");
        }
        try {
            ArrayList<Class<?>> columnTypes = new ArrayList<>();
            int columnCount = table.getColumnsCount();
            for (int i = 0; i < columnCount; i++) {
                columnTypes.add(table.getColumnType(i));
            }
            JSONArray array = new JSONArray(value);
            if (columnCount != array.length()) {
                throw new ParseException("Array size mismatch", 0);
            }
            FixedList newList = new FixedList(columnTypes);
            for (int i = 0; i < columnCount; i++) {
                Object object = array.get(i);
                if (object.equals(null)) {
                     newList.setColumnAt(i, null);
                } else if (columnTypes.get(i) == Integer.class) {
                    if (object.getClass() == Integer.class) {
                        newList.setColumnAt(i, object);
                    } else {
                        throw new ParseException(String.format("Type mismatch: %s expected, %s found",
                                columnTypes.get(i).toString(), object.getClass().toString()), i);
                    }
                } else if (columnTypes.get(i) == Long.class) {
                    if (object.getClass() == Long.class) {
                        newList.setColumnAt(i, object);
                    } else if (object.getClass() == Integer.class) {
                        newList.setColumnAt(i, Long.valueOf(((Integer) object).longValue()));
                    } else {
                        throw new ParseException(String.format("Type mismatch: %s expected, %s found",
                                columnTypes.get(i).toString(), object.getClass().toString()), i);
                    }
                } else if (columnTypes.get(i) == Byte.class) {
                    if (object.getClass() == Integer.class) {
                        Integer number = (Integer) object;
                        if (number > Byte.MAX_VALUE || number < Byte.MIN_VALUE) {
                            throw new ParseException(String.format("Type mismatch: %s expected, %s found",
                                    columnTypes.get(i).toString(), object.getClass().toString()), i);
                        }
                        newList.setColumnAt(i, Byte.valueOf(number.byteValue()));
                    } else {
                        throw new ParseException(String.format("Type mismatch: %s expected, %s found",
                                columnTypes.get(i).toString(), object.getClass().toString()), i);
                    }
                } else if (columnTypes.get(i) == Boolean.class) {
                    if (object.getClass() == Boolean.class) {
                        newList.setColumnAt(i, object);
                    } else {
                        throw new ParseException(String.format("Type mismatch: %s expected, %s found",
                                columnTypes.get(i).toString(), object.getClass().toString()), i);
                    }
                } else if (columnTypes.get(i) == Float.class) {
                    if (object.getClass() == Double.class) {
                        newList.setColumnAt(i, Float.valueOf(((Double) object).floatValue()));
                    } else if (object.getClass() == Integer.class) {
                        newList.setColumnAt(i, Float.valueOf(((Integer) object).floatValue()));
                    } else {
                        throw new ParseException(String.format("Type mismatch: %s expected, %s found",
                                columnTypes.get(i).toString(), object.getClass().toString()), i);
                    }
                } else if (columnTypes.get(i) == Double.class) {
                    if (object.getClass() == Double.class) {
                        newList.setColumnAt(i, object);
                    } else if (object.getClass() == Integer.class) {
                        newList.setColumnAt(i, Float.valueOf(((Integer) object).floatValue()));
                    } else {
                        throw new ParseException(String.format("Type mismatch: %s expected, %s found",
                                columnTypes.get(i).toString(), object.getClass().toString()), i);
                    }
                } else if (columnTypes.get(i) == String.class) {
                    if (object.getClass() != String.class) {
                        throw new ParseException(String.format("Type mismatch: %s expected, %s found",
                                columnTypes.get(i).toString(), object.getClass().toString()), i);
                    } else {
                        newList.setColumnAt(i, object);
                    }
                }
            }
            return newList;
        } catch (JSONException e) {
            throw new ParseException(e.getMessage(), 0);
        } catch (RuntimeException e) {
            throw new RuntimeException(String.format("Error parsing string %s", value), e);
        }
    }

    public String serialize(Table table, Storeable value) throws ColumnFormatException {
        int columnCount = table.getColumnsCount();
        Object[] objects = new Object[columnCount];
        for (int i = 0; i < columnCount; i++) {
            objects[i] = value.getColumnAt(i);
            if (objects[i] != null && objects[i].getClass() != table.getColumnType(i)) {
                throw new ColumnFormatException(String.format("Wrong format: %s expected, %s found",
                        table.getColumnType(i).toString(), objects[i].getClass().toString()));
            }
        }
        JSONArray array = new JSONArray(objects);
        return array.toString();
    }

    public FixedList createFor(Table table) {
        ArrayList<Class<?>> columnTypes = new ArrayList<>();
        int columnCount = table.getColumnsCount();
        for (int i = 0; i < columnCount; i++) {
            columnTypes.add(table.getColumnType(i));
        }
        return new FixedList(columnTypes);
    }

    public FixedList createFor(Table table, List<?> values) throws ColumnFormatException, IndexOutOfBoundsException {
        ArrayList<Class<?>> columnTypes = new ArrayList<>();
        int columnCount = table.getColumnsCount();
        for (int i = 0; i < columnCount; i++) {
            columnTypes.add(table.getColumnType(i));
        }
        FixedList newList = new FixedList(columnTypes);
        if (values.size() != columnCount) {
            throw new IndexOutOfBoundsException("Array size mismatch");
        }
        for (int i = 0; i < values.size(); i++) {
            newList.setColumnAt(i, values.get(i));
        }
        return newList;
    }
}
