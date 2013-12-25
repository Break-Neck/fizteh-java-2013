package ru.fizteh.fivt.students.lizaignatyeva.database;

import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;

import java.io.*;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MyTable implements Table {
    private boolean isValid;
    private Path globalDirectory;
    private String name;
    private HashMap<String, Storeable> data;
    private ThreadLocal<HashMap<String, Storeable>> uncommitedData;
    public final StoreableSignature columnTypes;
    private MyTableProvider tableProvider;
    private static final String CONFIG_FILE = "signature.tsv";

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

    private static final int BASE = 16;

    public MyTable(Path globalDirectory, String name, StoreableSignature columnTypes, MyTableProvider tableProvider) {
        this.isValid = true;
        this.globalDirectory = globalDirectory;
        this.name = name;
        this.columnTypes = columnTypes;
        this.tableProvider = tableProvider;
        this.data = new HashMap<>();
        this.uncommitedData = new ThreadLocal<HashMap<String, Storeable>>() {
            @Override
            protected HashMap<String, Storeable> initialValue() {
                return new HashMap<>();
            }
        };
    }

    private void checkValidness() {
        lock.readLock().lock();
        try {
            if (!isValid) {
                throw new IllegalStateException("This table has been deleted");
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getColumnsCount() {
        return columnTypes.getColumnsCount();
    }

    @Override
    public Class<?> getColumnType(int columnIndex) throws IndexOutOfBoundsException {
        return columnTypes.getColumnClass(columnIndex);
    }

    @Override
    public Storeable get(String key) {
        checkValidness();
        checkKey(key);
        if (uncommitedData.get().containsKey(key)) {
            return uncommitedData.get().get(key);
        }
        lock.readLock().lock();
        try {
            return data.get(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Storeable put(String key, Storeable value) {
        checkValidness();
        checkKey(key);
        checkValue(value);
        Storeable realValue = tableProvider.createFor(this);
        for (int index = 0; index < getColumnsCount(); index ++) {
            realValue.setColumnAt(index, value.getColumnAt(index));
        }
        Storeable result = null;
        if (uncommitedData.get().containsKey(key)) {
            result = uncommitedData.get().get(key);
        } else {
            lock.readLock().lock();
            try {
                if (data.containsKey(key)) {
                    result = data.get(key);
                }
            } finally {
                lock.readLock().unlock();
            }
        }
        uncommitedData.get().put(key, realValue);
        return result;
    }

    @Override
    public Storeable remove(String key) {
        checkValidness();
        checkKey(key);
        Storeable result = null;
        if (uncommitedData.get().containsKey(key)) {
            result = uncommitedData.get().get(key);
            lock.readLock().lock();
            try {
                if (data.containsKey(key)) {
                    uncommitedData.get().put(key, null);
                } else {
                    uncommitedData.get().remove(key);
                }
            } finally {
                lock.readLock().unlock();
            }
        } else {
            lock.readLock().lock();
            try {
                if (data.containsKey(key)) {
                    result = data.get(key);
                    uncommitedData.get().put(key, null);
                }
            } finally {
                lock.readLock().unlock();
            }
        }
        return result;
    }

    @Override
    public int size() {
        checkValidness();
        lock.readLock().lock();
        HashSet<String> keys = new HashSet<>();
        for (String key : data.keySet()) {
            keys.add(key);
        }
        lock.readLock().unlock();
        for (String key : uncommitedData.get().keySet()) {
            Storeable value = uncommitedData.get().get(key);
            if (value != null) {
                keys.add(key);
            } else {
                keys.remove(key);
            }
        }
        return keys.size();
    }

    @Override
    public int commit() {
        lock.writeLock().lock();
        try {
            checkValidness();
            int result = keysToCommit();
            for (String key : uncommitedData.get().keySet()) {
                Storeable value = uncommitedData.get().get(key);
                if (value == null) {
                    data.remove(key);
                } else {
                    data.put(key, value);
                }
            }
            try {
                write();
            } catch (IOException e) {
                throw new RuntimeException("Failed to write changes in table '" + name + "' to disk");
            }
            uncommitedData.set(new HashMap<String, Storeable>());
            return result;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public int rollback() {
        checkValidness();
        int result = keysToCommit();
        uncommitedData.set(new HashMap<String, Storeable>());
        return result;
    }

    public static boolean exists(Path globalDirectory, String name) {
        try {
            File path = globalDirectory.resolve(name).toFile();
            return path.isDirectory();
        } catch (Exception e) {
            return false;
        }
    }

    private static final HashMap<String, Class> SUPPORTED_CLASSES = new HashMap<>();
    private static final HashMap<Class, String> REVERSED_SUPPORTED_CLASSES = new HashMap<>();

    static {
        SUPPORTED_CLASSES.put("int", Integer.class);
        SUPPORTED_CLASSES.put("long", Long.class);
        SUPPORTED_CLASSES.put("byte", Byte.class);
        SUPPORTED_CLASSES.put("float", Float.class);
        SUPPORTED_CLASSES.put("double", Double.class);
        SUPPORTED_CLASSES.put("boolean", Boolean.class);
        SUPPORTED_CLASSES.put("String", String.class);

        for (String className : SUPPORTED_CLASSES.keySet()) {
            Class clazz = SUPPORTED_CLASSES.get(className);
            REVERSED_SUPPORTED_CLASSES.put(clazz, className);
        }
    }

    public static List<Class<?>> convert(List<String> classNames) {
        ArrayList<Class<?>> result = new ArrayList<>();
        for (String className : classNames) {
            if (!SUPPORTED_CLASSES.containsKey(className)) {
                throw new IllegalArgumentException("Class " + className + " is not supported");
            }
            result.add(SUPPORTED_CLASSES.get(className));
        }
        return result;
    }

    public static List<Class<?>> convert(String[] classNames) {
        return convert(Arrays.asList(classNames));
    }

    public static MyTable read(Path globalDirectory, String name, MyTableProvider tableProvider)
            throws IOException, DataFormatException {
        StoreableSignature columnTypes = readStoreableSignature(globalDirectory.resolve(name));
        MyTable table = new MyTable(globalDirectory, name, columnTypes, tableProvider);
        File path = globalDirectory.resolve(name).toFile();
        File[] subDirs = path.listFiles();
        if (subDirs == null) {
            return table;
        }
        for (File dir: subDirs) {
            if (dir.getName().equals(CONFIG_FILE)) {
                continue;
            }
            if (!dir.isDirectory() || !isValidDirectoryName(dir.getName())) {
                throw new DataFormatException("Table '" + name + "' contains strange file: '" + dir.getName() + "'");
            }
            table.readDirectory(dir);
        }
        return table;
    }

    private static StoreableSignature readStoreableSignature(Path directory) throws IOException, DataFormatException {
        File file = directory.resolve(CONFIG_FILE).toFile();
        if (!file.exists() || !file.isFile()) {
            throw new DataFormatException(CONFIG_FILE + " does not exist or is not a file");
        }
        ArrayList<Class<?>> classes = new ArrayList<>();
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNext()) {
                String className = scanner.next();
                if (!SUPPORTED_CLASSES.containsKey(className)) {
                    throw new DataFormatException("Class " + className + " is not supported");
                } else {
                    classes.add(SUPPORTED_CLASSES.get(className));
                }
            }
        }
        if (classes.size() == 0) {
            throw new DataFormatException("Empty " + CONFIG_FILE + " found");
        }
        return new StoreableSignature(classes);
    }

    private void readDirectory(File dir) throws DataFormatException, IOException {
        File[] filesInDirectory = dir.listFiles();
        if (filesInDirectory == null) {
            throw new DataFormatException("Empty directory");
        }
        boolean found = false;
        for (File file : filesInDirectory) {
            if (!file.isFile() || !isValidFileName(file.getName())) {
                throw new DataFormatException("Table '" + name + "' contains strange file: '" + file.getName() + "'");
            }
            found = true;
            readFile(file.getCanonicalPath(), dir.getName(), file.getName());
        }
        if (!found) {
            throw new DataFormatException("Empty directory");
        }
    }

    public void readFile(String filePath, String dirName, String fileName) throws DataFormatException, IOException {
        byte[] data = Files.readAllBytes(Paths.get(filePath));
        ByteBuffer buffer = ByteBuffer.wrap(data);
        boolean found = false;
        while (buffer.hasRemaining()) {
            try {
                readEntry(buffer, dirName, fileName);
                found = true;
            } catch (BufferUnderflowException e) {
                throw new DataFormatException("Table '" + name + "' contains corrupted file " + filePath, e);
            }
        }
        if (!found) {
            throw new DataFormatException("Empty file");
        }
    }

    private void readEntry(ByteBuffer buffer, String dirName, String fileName)
            throws BufferUnderflowException,
                   DataFormatException {
        int keyLength = buffer.getInt();
        if (keyLength > buffer.remaining() || keyLength < 0) {
            throw new DataFormatException("too long key buffer");
        }
        int valueLength = buffer.getInt();
        if (valueLength > buffer.remaining() || valueLength < 0) {
            throw new DataFormatException("too long value buffer");
        }
        byte[] keyBytes = new byte[keyLength];
        buffer.get(keyBytes);
        byte[] valueBytes = new byte[valueLength];
        buffer.get(valueBytes);
        String key = new String(keyBytes, StandardCharsets.UTF_8);
        if (!isValidKey(key, dirName, fileName)) {
            throw new DataFormatException("entry in a wrong file, key: " + key + ", file: "
                    + fileName + ", expected file: " + getFileName(key) + ", directory: " + dirName
                    + ", expected directory: " + getDirName(key));
        }
        String value = new String(valueBytes, StandardCharsets.UTF_8);
        if (data.containsKey(key)) {
            throw new DataFormatException("duplicating keys: " + key);
        }
        Storeable storeable;
        try {
            storeable = tableProvider.deserialize(this, value);
        } catch (ParseException e) {
            throw new DataFormatException("Incorrect data: failed to deserialize json", e);
        }
        data.put(key, storeable);
    }

    private static boolean isValidKey(String key, String dirName, String fileName) {
        return getDirName(key).equals(dirName) && getFileName(key).equals(fileName);
    }

    private static boolean isValidDirectoryName(String name) {
        for (int i = 0; i < BASE; ++i) {
            if (name.equals(Integer.toString(i) + ".dir")) {
                return true;
            }
        }
        return false;
    }

    private static boolean isValidFileName(String name) {
        for (int i = 0; i < BASE; ++i) {
            if (name.equals(Integer.toString(i) + ".dat")) {
                return true;
            }
        }
        return false;
    }

    public void write() throws IOException {
        lock.writeLock().lock();
        try {
            checkValidness();
            File path = globalDirectory.resolve(name).toFile();
            try {
                FileUtils.remove(path);
            } catch (Exception e) {
                //System.err.println("Error while updating database files: " + e.getMessage());
                //System.exit(1);
            }
            FileUtils.mkDir(path.getAbsolutePath());
            writeConfig();
            for (String key: data.keySet()) {
                String value = tableProvider.serialize(this, data.get(key));
                File directory = FileUtils.mkDir(path.getAbsolutePath()
                        + File.separator + getDirName(key));
                File file = FileUtils.mkFile(directory, getFileName(key));
                try (BufferedOutputStream outputStream = new BufferedOutputStream(
                        new FileOutputStream(file.getCanonicalPath(), true))) {
                    writeEntry(key, value, outputStream);
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    private String getClassName(Class clazz) {
        String className = REVERSED_SUPPORTED_CLASSES.get(clazz);
        if (className != null) {
            return className;
        } else {
            throw new IllegalArgumentException("Unsupported class: " + clazz.getCanonicalName());
        }
    }

    private void writeConfig() throws IOException {
        Path path = globalDirectory.resolve(name).resolve(CONFIG_FILE);
        boolean first = true;
        try (PrintWriter printWriter = new PrintWriter(path.toFile())) {
            for (Class clazz : columnTypes.columnClasses) {
                if (!first) {
                    printWriter.print(" ");
                }
                printWriter.print(getClassName(clazz));
                first = false;
            }
        }
    }

    private byte[] intToBytes(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }

    private void writeEntry(String key, String value, BufferedOutputStream outputStream) throws IOException {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);
        outputStream.write(intToBytes(keyBytes.length));
        outputStream.write(intToBytes(valueBytes.length));
        outputStream.write(keyBytes);
        outputStream.write(valueBytes);
    }

    public int keysToCommit() {
        lock.readLock().lock();
        try {
            int result = 0;
            for (String key: uncommitedData.get().keySet()) {
                if (!data.containsKey(key)) {
                    result ++;
                } else {
                    Storeable oldValue = data.get(key);
                    Storeable newValue = uncommitedData.get().get(key);
                    if (!oldValue.equals(newValue)) {
                        result++;
                    }
                }
            }
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    private static int getDirNumber(String key) {
        int number = key.getBytes()[0];
        number = Math.abs(number);
        return number % BASE;
    }

    private static int getFileNumber(String key) {
        int number = key.getBytes()[0];
        number = Math.abs(number);
        return number / BASE % BASE;
    }

    private static String getDirName(String key) {
        return String.format("%d.dir", getDirNumber(key));
    }

    private static String getFileName(String key) {
        return String.format("%d.dat", getFileNumber(key));
    }

    public void markAsDeleted() {
        lock.writeLock().lock();
        isValid = false;
        lock.writeLock().unlock();
    }

    private void checkKey(String key) {
        if (key == null || key.isEmpty() || key.contains("\0") || key.matches(".*\\s.*")) {
            throw new IllegalArgumentException("key is invalid");
        }
    }

    private void checkValue(Storeable value) {
        if (value == null) {
            throw new IllegalArgumentException("value is null");
        }
        try {
            for (int index = 0; index < getColumnsCount(); index ++) {
                if (!(value.getColumnAt(index) == null
                        || value.getColumnAt(index).getClass().equals(columnTypes.getColumnClass(index)))) {
                    throw new ColumnFormatException("incorrect type of value for column number " + index);
                }
            }
        } catch (IndexOutOfBoundsException e) {
            throw new ColumnFormatException("value contains less columns", e);
        }
        try {
            value.getColumnAt(getColumnsCount());
        } catch (IndexOutOfBoundsException e) {
            return;
        }
        throw new ColumnFormatException("value contains more columns");
    }

    public HashMap<String, Storeable> getLocalChanges() {
        return uncommitedData.get();
    }

    public void setLocalChanges(HashMap<String, Storeable> changes) {
        uncommitedData.set(changes);
    }
}
