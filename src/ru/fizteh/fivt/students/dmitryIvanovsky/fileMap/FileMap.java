package ru.fizteh.fivt.students.dmitryIvanovsky.fileMap;

import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;
import ru.fizteh.fivt.students.dmitryIvanovsky.shell.CommandShell;

import java.nio.charset.StandardCharsets;
import java.util.Vector;
import java.util.StringTokenizer;
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.fizteh.fivt.students.dmitryIvanovsky.fileMap.FileMapUtils.*;

public class FileMap implements Table {

    private final Path pathDb;
    private final CommandShell mySystem;
    String nameTable;
    Map<String, Storeable> tableData = new HashMap<>();
    Map<String, Storeable> changeTable = new HashMap<>();
    boolean existDir = false;
    boolean tableDrop = false;
    FileMapProvider parent;
    List<Class<?>> columnType = new ArrayList<Class<?>>();

    public FileMap(Path pathDb, String nameTable, FileMapProvider parent) throws Exception {
        this.nameTable = nameTable;
        this.pathDb = pathDb;
        this.parent = parent;
        this.mySystem = new CommandShell(pathDb.toString(), false, false);

        File theDir = new File(String.valueOf(pathDb.resolve(nameTable)));
        if (!theDir.exists()) {
            try {
                mySystem.mkdir(new String[]{pathDb.resolve(nameTable).toString()});
                existDir = true;
            } catch (Exception e) {
                e.addSuppressed(new ErrorFileMap("I can't create a folder table " + nameTable));
                throw e;
            }
        }

        loadTypeFile(pathDb);

        try {
            loadTable(nameTable);
        } catch (Exception e) {
            e.addSuppressed(new ErrorFileMap("Format error storage table " + nameTable));
            throw e;
        }
    }

    private String readFileTsv(String fileName) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new FileReader(new File(fileName).getAbsoluteFile()))) {
            String s;
            while ((s = in.readLine()) != null) {
                sb.append(s);
            }
        } catch (Exception e) {
            throw new IOException("not found signature.tsv", e);
        }
        if (sb.length() == 0) {
            throw new IOException("tsv file is empty");
        }
        return sb.toString();
    }

    private void writeFileTsv() throws FileNotFoundException {
        Path pathTsv = pathDb.resolve(nameTable).resolve("signature.tsv");
        try (PrintWriter out = new PrintWriter(pathTsv.toFile().getAbsoluteFile())) {
            for (int i = 0; i < columnType.size(); ++i) {
                out.print(convertClassToString(columnType.get(i)));
                if (i != columnType.size() - 1) {
                    out.print(" ");
                }
            }
        }
    }

    private void loadTypeFile(Path pathDb) throws IOException {
        String fileStr = readFileTsv(pathDb.resolve(nameTable).resolve("signature.tsv").toString());
        StringTokenizer token = new StringTokenizer(fileStr);
        while (token.hasMoreTokens()) {
            String tok = token.nextToken();
            Class<?> type = convertStringToClass(tok);
            if (type == null) {
                throw new IllegalArgumentException(String.format("wrong type %s", tok));
            }
            columnType.add(type);
        }
    }

    public FileMap(Path pathDb, String nameTable, FileMapProvider parent, List<Class<?>> columnType) throws Exception {
        this.nameTable = nameTable;
        this.pathDb = pathDb;
        this.parent = parent;
        this.columnType = columnType;
        this.mySystem = new CommandShell(pathDb.toString(), false, false);

        File theDir = new File(String.valueOf(pathDb.resolve(nameTable)));
        if (!theDir.exists()) {
            try {
                mySystem.mkdir(new String[]{pathDb.resolve(nameTable).toString()});
                existDir = true;
            } catch (Exception e) {
                e.addSuppressed(new ErrorFileMap("I can't create a folder table " + nameTable));
                throw e;
            }
        }

        writeFileTsv();

        try {
            loadTable(nameTable);
        } catch (Exception e) {
            e.addSuppressed(new ErrorFileMap("Format error storage table " + nameTable));
            throw e;
        }
    }

    private void loadTable(String nameMap) throws Exception {
        File currentFileMap = pathDb.resolve(nameMap).toFile();
        if (!currentFileMap.isDirectory()) {
            throw new ErrorFileMap(currentFileMap.getAbsolutePath() + " isn't directory");
        }

        File[] listFileMap = currentFileMap.listFiles();

        if (listFileMap == null || listFileMap.length == 0) {
            throw new ErrorFileMap(pathDb + " empty table");
        }

        for (File nameDir : listFileMap) {
            if (nameDir.getName().equals("signature.tsv")) {
                continue;
            }
            if (!nameDir.isDirectory()) {
                throw new ErrorFileMap(nameDir.getAbsolutePath() + " isn't directory");
            }
            String nameStringDir = nameDir.getName();
            Pattern p = Pattern.compile("(^[0-9].dir$)|(^1[0-5].dir$)");
            Matcher m = p.matcher(nameStringDir);

            if (!(m.matches() && m.start() == 0 && m.end() == nameStringDir.length())) {
                throw new ErrorFileMap(nameDir.getAbsolutePath() + " wrong folder name");
            }

            File[] listNameDir = nameDir.listFiles();
            if (listNameDir == null || listNameDir.length == 0) {
                throw new ErrorFileMap(nameDir.getAbsolutePath() + " empty dir");
            }
            for (File randomFile : listNameDir) {

                p = Pattern.compile("(^[0-9].dat$)|(^1[0-5].dat$)");
                m = p.matcher(randomFile.getName());
                int lenRandomFile = randomFile.getName().length();
                if (!(m.matches() && m.start() == 0 && m.end() == lenRandomFile)) {
                    throw new ErrorFileMap(randomFile.getAbsolutePath() + " invalid file name");
                }

                try {
                    loadTableFile(randomFile, tableData, nameDir.getName());
                } catch (Exception e) {
                    e.addSuppressed(new ErrorFileMap("Error in file " + randomFile.getAbsolutePath()));
                    throw e;
                }
            }
        }
    }

    public void loadTableFile(File randomFile, Map<String, Storeable> dbMap, String nameDir) throws Exception {
        if (randomFile.isDirectory()) {
            throw new ErrorFileMap("data file can't be a directory");
        }
        int intDir = FileMapUtils.getCode(nameDir);
        int intFile = FileMapUtils.getCode(randomFile.getName());

        RandomAccessFile dbFile = null;
        Exception error = null;
        try {
            try {
                dbFile = new RandomAccessFile(randomFile, "rw");
            } catch (Exception e) {
                throw new ErrorFileMap("file doesn't open");
            }
            if (dbFile.length() == 0) {
                throw new ErrorFileMap("file is clear");
            }
            dbFile.seek(0);

            byte[] arrayByte;
            Vector<Byte> vectorByte = new Vector<Byte>();
            long separator = -1;

            while (dbFile.getFilePointer() != dbFile.length()) {
                byte currentByte = dbFile.readByte();
                if (currentByte == '\0') {
                    int point1 = dbFile.readInt();
                    if (separator == -1) {
                        separator = point1;
                    }
                    long currentPoint = dbFile.getFilePointer();

                    while (dbFile.getFilePointer() != separator) {
                        if (dbFile.readByte() == '\0') {
                            break;
                        }
                    }

                    int point2;
                    if (dbFile.getFilePointer() == separator) {
                        point2 = (int) dbFile.length();
                    } else {
                        point2 = dbFile.readInt();
                    }

                    dbFile.seek(point1);

                    arrayByte = new byte[point2 - point1];
                    dbFile.readFully(arrayByte);
                    String value = new String(arrayByte, StandardCharsets.UTF_8);

                    arrayByte = new byte[vectorByte.size()];
                    for (int i = 0; i < vectorByte.size(); ++i) {
                        arrayByte[i] = vectorByte.elementAt(i).byteValue();
                    }
                    String key = new String(arrayByte, StandardCharsets.UTF_8);

                    if (FileMapUtils.getHashDir(key) != intDir || FileMapUtils.getHashFile(key) != intFile) {
                        throw new ErrorFileMap("wrong key in the file");
                    }
                    dbMap.put(key, parent.deserialize(this, value));
                    vectorByte.clear();
                    dbFile.seek(currentPoint);
                } else {
                    vectorByte.add(currentByte);
                }
            }
        } catch (Exception e) {
            error = e;
            throw error;
        } finally {
            try {
                dbFile.close();
            } catch (Exception e) {
                if (error != null) {
                    error.addSuppressed(e);
                }
            }
        }
    }

    public void unloadTable() throws Exception {
        if (tableDrop) {
            throw new IllegalStateException("table was deleted");
        }
        mySystem.rm(new String[]{pathDb.resolve(nameTable).toString()});
        existDir = false;
        try {
            mySystem.mkdir(new String[]{pathDb.resolve(nameTable).toString()});
        } catch (Exception e) {
            e.addSuppressed(new ErrorFileMap("I can't create a folder table " + pathDb.resolve(nameTable).toString()));
            throw e;
        }
        existDir = true;
        closeTable();
    }

    private void closeTable() throws Exception {
        writeFileTsv();
        if (tableData.isEmpty()) {
            return;
        }

        Map<String, Storeable>[][] arrayMap = new HashMap[16][16];
        boolean[] useDir = new boolean[16];
        for (String key : tableData.keySet()) {

            int ndirectory = FileMapUtils.getHashDir(key);
            int nfile = FileMapUtils.getHashFile(key);

            if (arrayMap[ndirectory][nfile] == null) {
                arrayMap[ndirectory][nfile] = new HashMap<String, Storeable>();
            }
            arrayMap[ndirectory][nfile].put(key, tableData.get(key));
            useDir[ndirectory] = true;
        }
        for (int i = 0; i < 16; ++i) {
            if (useDir[i]) {
                Integer numDir = i;
                Path tmp = pathDb.resolve(nameTable).resolve(numDir.toString() + ".dir");
                try {
                    mySystem.mkdir(new String[]{tmp.toString()});
                } catch (Exception e) {
                    e.addSuppressed(new ErrorFileMap("I can't create a folder table " + tmp.toString()));
                    throw e;
                }
            }
        }
        for (int i = 0; i < 16; ++i) {
            if (!useDir[i]) {
                continue;
            }
            for (int j = 0; j < 16; ++j) {
                Integer numDir = i;
                Integer numFile = j;
                Path tmp = pathDb.resolve(nameTable).resolve(numDir.toString() + ".dir");
                closeTableFile(tmp.resolve(numFile.toString() + ".dat").toFile(), arrayMap[i][j]);
            }
        }
    }

    public void closeTableFile(File randomFile, Map<String, Storeable> curMap) throws Exception {
        if (curMap == null || curMap.isEmpty()) {
            return;
        }
        RandomAccessFile dbFile = null;
        Exception error = null;
        try {
            dbFile = new RandomAccessFile(randomFile, "rw");
            dbFile.setLength(0);
            dbFile.seek(0);
            int len = 0;

            for (String key : curMap.keySet()) {
                len += key.getBytes(StandardCharsets.UTF_8).length + 1 + 4;
            }

            for (String key : curMap.keySet()) {
                dbFile.write(key.getBytes(StandardCharsets.UTF_8));
                dbFile.writeByte(0);
                dbFile.writeInt(len);

                long point = dbFile.getFilePointer();

                dbFile.seek(len);
                Storeable valueStoreable = curMap.get(key);
                String value = parent.serialize(this, valueStoreable);
                dbFile.write(value.getBytes(StandardCharsets.UTF_8));
                len += value.getBytes(StandardCharsets.UTF_8).length;

                dbFile.seek(point);
            }
        } catch (Exception e) {
            error = e;
            throw error;
        } finally {
            try {
                dbFile.close();
            } catch (Exception e) {
                if (error != null) {
                    error.addSuppressed(e);
                }
            }
        }
    }

    public String getName() {
        if (tableDrop) {
            throw new IllegalStateException("table was deleted");
        }
        return nameTable;
    }

    public int changeKey() {
        if (tableDrop) {
            throw new IllegalStateException("table was deleted");
        }
        return changeTable.size();
    }

    public Storeable put(String key, Storeable value) throws ColumnFormatException {
        checkArg(key);
        if (value == null) {
            throw new IllegalArgumentException("value can't be null");
        }
        if (tableDrop) {
            throw new IllegalStateException("table was deleted");
        }
        FileMapStoreable st = null;
        try {
            st = (FileMapStoreable) value;
        } catch (ClassCastException e) {

            boolean valueMoreSize = false;
            try {
                value.getColumnAt(columnType.size());
                valueMoreSize = true;
            } catch (Exception err) {
                valueMoreSize = false;
            }

            if (valueMoreSize) {
                throw new ColumnFormatException("this Storeable can't be use in this table");
            }

            int index = 0;

            while (true) {
                try {
                    if (columnType.get(index) == Integer.class) {
                        value.getIntAt(index);
                    } else if (columnType.get(index) == Long.class) {
                        value.getLongAt(index);
                    } else if (columnType.get(index) == Byte.class) {
                        value.getByteAt(index);
                    } else if (columnType.get(index) == Float.class) {
                        value.getFloatAt(index);
                    } else if (columnType.get(index) == Double.class) {
                        value.getDoubleAt(index);
                    } else if (columnType.get(index) == Boolean.class) {
                        value.getBooleanAt(index);
                    } else if (columnType.get(index) == String.class) {
                        value.getStringAt(index);
                    } else {
                        throw new ColumnFormatException("in ColumnType isn't provide type");
                    }

                    ++index;
                } catch (IndexOutOfBoundsException err) {
                    if (index != columnType.size()) {
                        throw new ColumnFormatException("this Storeable can't be use in this table");
                    }
                    break;
                }
            }

            st = null;
        }

        if (st != null && !st.messageEqualsType(columnType).isEmpty()) {
            throw new ColumnFormatException(st.messageEqualsType(columnType));
        }

        if (changeTable.containsKey(key)) {
            Storeable newValue = changeTable.get(key);
            if (newValue == null) {
                Storeable oldValue = tableData.get(key);
                if (parent.serialize(this, oldValue).equals(parent.serialize(this, value))) {
                    changeTable.remove(key);
                    return null;
                } else {
                    changeTable.put(key, value);
                    return null;
                }
            } else {
                Storeable oldValue = changeTable.get(key);
                changeTable.put(key, value);
                return oldValue;
            }
        } else {
            if (tableData.containsKey(key)) {
                Storeable oldValue = tableData.get(key);
                if (!parent.serialize(this, oldValue).equals(parent.serialize(this, value))) {
                    changeTable.put(key, value);
                }
                return oldValue;
            } else {
                changeTable.put(key, value);
                return null;
            }
        }
    }

    public void setDrop() {
        tableDrop = true;
    }

    public Storeable get(String key) {
        checkArg(key);
        if (tableDrop) {
            throw new IllegalStateException("table was deleted");
        }
        if (changeTable.containsKey(key)) {
            Storeable newValue = changeTable.get(key);
            if (newValue == null) {
                return null;
            } else {
                Storeable value = changeTable.get(key);
                return value;
            }
        } else {
            if (tableData.containsKey(key)) {
                Storeable value = tableData.get(key);
                return value;
            } else {
                return null;
            }
        }
    }

    public Storeable remove(String key) {
        checkArg(key);
        if (tableDrop) {
            throw new IllegalStateException("table was deleted");
        }
        if (changeTable.containsKey(key)) {
            Storeable newValue = changeTable.get(key);
            if (tableData.containsKey(key)) {
                if (newValue == null) {
                    return null;
                } else {
                    Storeable value = changeTable.get(key);
                    changeTable.put(key, null);
                    return value;
                }
            } else {
                changeTable.remove(key);
                return newValue;
            }
        } else {
            if (tableData.containsKey(key)) {
                Storeable value = tableData.get(key);
                changeTable.put(key, null);
                return value;
            } else {
                return null;
            }
        }
    }

    public int size() {
        if (tableDrop) {
            throw new IllegalStateException("table was deleted");
        }
        int size = tableData.size();
        for (String key : changeTable.keySet()) {
            if (tableData.containsKey(key)) {
                if (changeTable.get(key) == null) {
                    --size;
                }
            } else {
                if (changeTable.get(key) != null) {
                    ++size;
                }
            }
        }
        return size;
    }

    public int commit() {
        if (tableDrop) {
            throw new IllegalStateException("table was deleted");
        }
        for (String key : changeTable.keySet()) {
            Storeable value = changeTable.get(key);
            if (value == null) {
                tableData.remove(key);
            } else {
                tableData.put(key, changeTable.get(key));
            }
        }
        int count = changeTable.size();
        changeTable.clear();
        try {
            unloadTable();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return count;
    }

    public int rollback() {
        if (tableDrop) {
            throw new IllegalStateException("table was deleted");
        }
        int res = changeTable.size();
        changeTable.clear();
        return res;
    }

    @Override
    public int getColumnsCount() {
        if (tableDrop) {
            throw new IllegalStateException("table was deleted");
        }
        return columnType.size();
    }

    @Override
    public Class<?> getColumnType(int columnIndex) throws IndexOutOfBoundsException {
        if (tableDrop) {
            throw new IllegalStateException("table was deleted");
        }
        return columnType.get(columnIndex);
    }

}
