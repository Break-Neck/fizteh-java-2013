package ru.fizteh.fivt.students.baldindima.junit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.RandomAccessFile;

import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;
import ru.fizteh.fivt.storage.structured.TableProvider;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DataBase implements Table, AutoCloseable {
    private String dataBaseDirectory;
    private TableProvider provider;
    private List<Class<?>> types;
    private Map<Integer, DataBaseFile> files = new WeakHashMap<>();
    private File sizeFile;
    private int sizeTable;

    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);
    public Lock readLock = readWriteLock.readLock();
    public Lock writeLock = readWriteLock.writeLock();

    private volatile boolean isClosed = false;
    private ThreadLocal<HashMap<String, String>> changes = new ThreadLocal<HashMap<String, String>>() {

        public HashMap<String, String> initialValue() {
            return new HashMap<String, String>();
        }
    };

    private void checkNames(String[] fileList, String extension) throws IOException {
        for (String fileNumber : fileList) {
            if ((fileNumber.equals("signature.tsv")) || (fileNumber.equals("size.tsv"))) {
                continue;

            }
            String[] nameFile = fileNumber.split("\\.");
            if ((nameFile.length != 2)
                    || !nameFile[1].equals(extension)) {
                throw new IOException(dataBaseDirectory + " wrong file " + fileNumber);
            }
            int intName;
            try {
                intName = Integer.parseInt(nameFile[0]);
            } catch (NumberFormatException e) {
                throw new IOException(dataBaseDirectory + " wrong name of file" + fileNumber);
            }
            if ((intName < 0) || (intName > 15))
                throw new IOException(dataBaseDirectory + " wrong name of file" + fileNumber);
        }
    }

    private void checkCorrectionDirectory(String directoryName) throws IOException {
        File file = new File(directoryName);
        if (file.isFile()) {
            throw new IOException(directoryName + " isn't a directory!");
        }
        if (file.list().length <= 0) {
            throw new IOException(directoryName + " is empty");
        }
        checkNames(file.list(), "dat");
        for (String fileName : file.list()) {
            File fileHelp = new File(directoryName, fileName);
            if (fileHelp.isDirectory()) {
                throw new IOException(directoryName + File.separator + fileName + " isn't a file!");
            }
            if (fileHelp.length() <= 0) {
                throw new IOException(directoryName + File.separator + fileName + " is empty!");
            }
        }
    }

    private void checkCorrection() throws IOException {
        File file = new File(dataBaseDirectory);
        if (!file.exists()) {
            throw new IOException(dataBaseDirectory + " isn't exist");
        }
        if (file.isFile()) {
            throw new IOException(dataBaseDirectory + " isn't exist");
        }
        if (file.list().length <= 0) {
            throw new IOException(dataBaseDirectory + " is empty");
        }
        checkNames(file.list(), "dir");
        for (String fileNumber : file.list()) {
            if ((!fileNumber.equals("signature.tsv")) && (!fileNumber.equals("size.tsv"))) {
                checkCorrectionDirectory(dataBaseDirectory + File.separator + fileNumber);
            }

        }


    }

    public DataBase(String nameDirectory, TableProvider nProvider, List<Class<?>> nTypes) throws IOException {

        dataBaseDirectory = nameDirectory;
        provider = nProvider;

        types = nTypes;
        BaseSignature.setBaseSignature(dataBaseDirectory, types);


        checkCorrection();
        loadDataBase();
    }

    public DataBase(String nameDirectory, TableProvider nProvider) throws IOException {

        dataBaseDirectory = nameDirectory;
        provider = nProvider;

        types = BaseSignature.getBaseSignature(dataBaseDirectory);


        checkCorrection();
        loadDataBase();
    }


    private String getFullName(int nDir, int nFile) {
        return dataBaseDirectory + File.separator + Integer.toString(nDir)
                + ".dir" + File.separator + Integer.toString(nFile) + ".dat";
    }

    private int realSizeTable() throws IOException {
        int sizeTable = 0;
        for (int i = 0; i < 16; ++i) {
            for (int j = 0; j < 16; ++j) {
                int nFile = j;
                int nDir = i;
                files.put(nDir * 16 + nFile, new DataBaseFile(getFullName(i, j), i, j, provider, this));
                if (files.get(nDir * 16 + nFile) != null){
                	sizeTable += files.get(nDir * 16 + nFile).mapFromFile.size();
                }
                
            }
        }
        return sizeTable;
    }

    private void loadDataBase() throws IOException {

        sizeFile = new File(dataBaseDirectory, "size.tsv");
        if ((!sizeFile.exists())) {
            sizeTable = realSizeTable();
            if (!sizeFile.createNewFile()) {
                throw new IllegalArgumentException("cannot create a size file");
            }
            try (PrintStream printStream = new PrintStream(sizeFile)) {
                printStream.print(sizeTable);
            }
        } else {
            try (Scanner scanner = new Scanner(sizeFile)) {
                if (scanner.hasNextInt()) {
                    sizeTable = scanner.nextInt();
                } else {
                    sizeTable = realSizeTable();
                    try (PrintStream printStream = new PrintStream(sizeFile)) {
                        printStream.print(sizeTable);
                    }
                }

            }
        }
    }

    private void deleteEmptyDirectory(final String name) throws IOException {
        File file = new File(dataBaseDirectory + File.separator + name);
        if (file.exists()) {
            if (file.list().length == 0) {
                if (!file.delete()) {
                    throw new IOException("Cannot delete a directory!");
                }
            }
        }
    }

    public void drop() throws IOException {
        for (byte i = 0; i < 16; ++i) {
            for (byte j = 0; j < 16; ++j) {
                File file = new File(getFullName(i, j));
                if (file.exists()) {
                    if (!file.isFile()) {
                        throw new IOException("It isn't a file!");
                    }
                    if (!file.delete()) {
                        throw new IOException("Cannot delete a file!");
                    }
                }
            }
            deleteEmptyDirectory(Integer.toString(i) + ".dir");
        }
        if (!new File(dataBaseDirectory, "signature.tsv").delete()) {
            throw new IOException("Cannot delete a file!");
        }
        if (!new File(dataBaseDirectory, "size.tsv").delete()) {
                throw new IOException("Cannot delete a file!");
        }
    }


    private String getFromOld(String keyString) {
        String result;
        int nDir = getnDir(keyString);
        int nFile = getnFile(keyString);
        int nFileInMap = getnFileInMap(keyString);
        readLock.lock();
        try {
        	
            if ((files.containsKey(nFileInMap)) && (files.get(nFileInMap) != null) && (files.get(nFileInMap).mapFromFile != null)) {
               	result = files.get(nFileInMap).mapFromFile.get(keyString);
            } else {
                try {
                    files.put(nFileInMap, new DataBaseFile(getFullName(nDir, nFile), nDir, nFile, provider, this));
                    if (files.get(nFileInMap) != null) {
                    	result = files.get(nFileInMap).mapFromFile.get(keyString);
                    } else {
                    	result = null;
                    }
                    
                } catch (IOException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        } finally {
            readLock.unlock();
        }
        return result;
    }

    public Storeable get(String keyString) {
        checkClosed();
        checkString(keyString);
        String result;
        if (changes.get().containsKey(keyString)) {
            result = changes.get().get(keyString);
        } else {
            result = getFromOld(keyString);
        }

        return JSONClass.deserialize(this, result);
    }

    public Storeable put(String keyString, Storeable storeable) {
        checkClosed();
        checkString(keyString);
        String valueString = JSONClass.serialize(this, storeable);
        //checkString(valueString);
        Storeable result = get(keyString);
        changes.get().put(keyString, valueString);
        return result;
    }

    public Storeable remove(String keyString) {
        checkClosed();
        checkString(keyString);
        Storeable result = get(keyString);
        changes.get().put(keyString, null);
        return result;
    }

    public int countCommits() {
        int count = 0;
        readLock.lock();
        try {
            for (String change : changes.get().keySet()) {
                String s = getFromOld(change);
                if (!((s == null) && (changes.get().get(change) == null) ||
                        ((s != null) && (changes.get().get(change) != null) &&
                                s.equals(changes.get().get(change))))) {
                    ++count;
                }
            }
        } finally {
            readLock.unlock();
        }
        return count;
    }

    public int size() {
        checkClosed();
        readLock.lock();
        try {
            int count = 0;
            for (Map.Entry<String, String> change : changes.get().entrySet()) {
                if (change.getValue() == null && getFromOld(change.getKey()) != null) {
                    --count;
                } else if (change.getValue() != null && getFromOld(change.getKey()) == null) {
                    ++count;
                }
            }
            return sizeTable + count;
        } finally {
            readLock.unlock();
        }
    }

    private int getnDir(String key) {
        return Math.abs(key.getBytes()[0]) % 16;
    }

    private int getnFile(String key) {
        return Math.abs((key.getBytes()[0] / 16) % 16);
    }

    private int getnFileInMap(String key) {
        return (getnDir(key) * 16 + getnFile(key));
    }

    public int commit() throws FileNotFoundException, IOException {

        checkClosed();
        ThreadLocal<Map<Integer, Map<String, String>>> newDataBase =
                new ThreadLocal<Map<Integer, Map<String, String>>>() {
                    protected Map<Integer, Map<String, String>> initialValue() {
                        return new HashMap<>();
                    }
                };
        ThreadLocal<Set<Integer>> update = new ThreadLocal<Set<Integer>>() {

            protected Set<Integer> initialValue() {
                return new HashSet<>();
            }
        };

        int count = countCommits();
        sizeTable = size();

        try (PrintStream printStream = new PrintStream(sizeFile)) {
            printStream.print(sizeTable);
        }
        writeLock.lock();
        try {
            for (Map.Entry<String, String> change : changes.get().entrySet()) {
                int nFileInMap = getnFileInMap(change.getKey());
                update.get().add(nFileInMap);
                if (newDataBase.get().get(nFileInMap) == null) {
                    newDataBase.get().put(nFileInMap, new HashMap<String, String>());
                }
                newDataBase.get().get(nFileInMap).put(change.getKey(), change.getValue());
            }
            for (Integer nfile : update.get()) {
                DataBaseFile updateFile = putNewValues(nfile, newDataBase.get().get(nfile));
                updateFile.write();
            }
        } finally {
            writeLock.unlock();
        }

        changes.get().clear();
        return count;
    }

    private DataBaseFile getOldMap(int nFileInMap) throws IOException {
        if (!files.containsKey(nFileInMap)) {
            files.put(nFileInMap, new DataBaseFile(getFullName(nFileInMap / 16, nFileInMap % 16),
                    nFileInMap / 16, nFileInMap % 16, provider, this));
        }
        return files.get(nFileInMap);
    }

    private void mergeNewAndOld(DataBaseFile newValuesMap, Map<String, String> changes) {
        for (Map.Entry<String, String> change : changes.entrySet()) {
            if (change.getValue() == null) {
                newValuesMap.mapFromFile.remove(change.getKey());
            } else {
                newValuesMap.mapFromFile.put(change.getKey(), change.getValue());
            }
        }
    }

    public DataBaseFile putNewValues(int nFileInMap, Map<String, String> changes) throws IOException {
        DataBaseFile newValuesMap = getOldMap(nFileInMap);
        mergeNewAndOld(newValuesMap, changes);
        files.put(nFileInMap, newValuesMap);
        return newValuesMap;
    }

    public int rollback() {
        checkClosed();
        int res = countCommits();
        changes.get().clear();
        return res;

    }

    public Storeable putStoreable(String keyStr, String valueStr) throws ParseException {
        return put(keyStr, provider.deserialize(this, valueStr));
    }

    public String getName() {
        checkClosed();
        return new File(dataBaseDirectory).getName();
    }

    private void checkString(String str) {
        if ((str == null) || (str.trim().length() == 0)) {
            throw new IllegalArgumentException("Wrong key!");
        }
        for (int i = 0; i < str.length(); ++i) {
            if (Character.isWhitespace(str.charAt(i))) {
                throw new IllegalArgumentException("Wrong key!");
            }
        }


    }

    public int getColumnsCount() {
        checkClosed();
        return types.size();
    }

    public Class<?> getColumnType(int columnIndex)
            throws IndexOutOfBoundsException {
        checkClosed();
        if ((columnIndex < 0) || (columnIndex >= types.size())) {
            throw new IndexOutOfBoundsException("wrong columnIndex");
        }
        return types.get(columnIndex);
    }

    private void checkClosed() {
        if (isClosed) {
            throw new IllegalStateException("call for closed object");
        }
    }

    public void close() {
        if (!isClosed) {
            rollback();
            isClosed = true;
        }
    }
}