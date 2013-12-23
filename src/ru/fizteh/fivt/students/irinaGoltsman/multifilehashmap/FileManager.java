package ru.fizteh.fivt.students.irinaGoltsman.multifilehashmap;

import ru.fizteh.fivt.students.irinaGoltsman.multifilehashmap.tools.ColumnTypes;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class FileManager {

    private static class LineOfDB {
        public long length = 0;
        public String key = "";
        public String value = "";

        public LineOfDB() {
        }

        public LineOfDB(long length, String key, String value) {
            this.length = length;
            this.key = key;
            this.value = value;
        }
    }

    private static class SizeOfTable {
        public int removedKeys = 0;
        public int changes = 0;
        public int addedKeys = 0;

        public SizeOfTable() {
        }

        public SizeOfTable(int removed, int added, int ch) {
            this.removedKeys = removed;
            this.addedKeys = added;
            this.changes = ch;
        }
    }

    private static void checkKeyOnRightHashCode(String key, int originalDirIndex, int originalFileIndex)
            throws IOException {
        int hashCode = key.hashCode();
        int indexOfDir = hashCode % 16;
        if (indexOfDir < 0) {
            indexOfDir *= -1;
        }
        int indexOfDat = hashCode / 16 % 16;
        if (indexOfDat < 0) {
            indexOfDat *= -1;
        }
        if (indexOfDir != originalDirIndex || indexOfDat != originalFileIndex) {
            throw new IOException(String.format("wrong key '%s': it should be in %d.dir in %d.dat, "
                    + "but it is in %d.dir in %d.dat", key, indexOfDir,
                    indexOfDat, originalDirIndex, originalFileIndex));
        }
    }

    private static LineOfDB readLineOfDatFile(RandomAccessFile datFile, long inputLength,
                                              int dirIndex, int fileIndex) throws IOException {
        long length = inputLength;
        int lengthOfKey;
        int lengthOfValue;
        LineOfDB empty = new LineOfDB();
        try {
            lengthOfKey = datFile.readInt();
            lengthOfValue = datFile.readInt();
        } catch (IOException e) {
            throw new IOException("Wrong format of db: " + e.getMessage());
        }
        length -= 8;
        if (lengthOfKey <= 0 || lengthOfValue <= 0) {
            throw new IOException("Wrong format of db: length of key and length of value must be positive integers.");
        }
        if (lengthOfKey > length) {
            throw new IOException("Wrong format of db: length of key ​​do not match content.");
        }
        byte[] bytesOfKey = readKeyOrValue(datFile, lengthOfKey);
        length -= lengthOfKey;
        if (lengthOfValue > length) {
            System.err.println("Wrong format of db: length of value ​​do not match content.");
            return empty;
        }
        byte[] bytesOfValue = readKeyOrValue(datFile, lengthOfValue);
        length -= lengthOfValue;
        String key;
        String value;
        try {
            key = new String(bytesOfKey, "UTF-8");
            value = new String(bytesOfValue, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            System.err.println(e);
            return empty;
        }
        if (key.contains("\\s")) {
            throw new IOException("key contains whitespace symbol");
        }
        checkKeyOnRightHashCode(key, dirIndex, fileIndex);
        long lengthOfLine = inputLength - length;
        return new LineOfDB(lengthOfLine, key, value);
    }

    private static byte[] readKeyOrValue(RandomAccessFile datFile, int length) throws IOException {
        byte[] bytes = new byte[length];
        int countOfBytesWasRead = 0;
        while (true) {
            try {
                countOfBytesWasRead = datFile.read(bytes, countOfBytesWasRead, length - countOfBytesWasRead);
            } catch (IOException e) {
                throw new IOException("Wrong format of db: " + e.getMessage());
            }
            if (countOfBytesWasRead == length) {
                break;
            }
            if (countOfBytesWasRead == -1) {
                throw new IOException("Error while reading key or value");
            }
        }
        return bytes;
    }

    private static void readDatFileFromDisk(RandomAccessFile datFile, HashMap<String, String> storage,
                                            int dirIndex, int fileIndex) throws IOException {
        long length;
        length = datFile.length();
        if (length == 0) {
            return;
        }
        datFile.seek(0);
        while (length > 0) {
            LineOfDB line = readLineOfDatFile(datFile, length, dirIndex, fileIndex);
            length -= line.length;
            storage.put(line.key, line.value);
        }
        datFile.close();
    }

    public static void readDBFromDisk(File tableDirectory, HashMap<String, String> tableStorage) throws IOException {
        if (!tableDirectory.exists() || tableDirectory.isFile()) {
            throw new IOException(tableDirectory + ": not directory or not exist");
        }
        for (int index = 0; index < 16; index++) {
            String currentDirectoryName = index + ".dir";
            File currentDirectory = new File(tableDirectory, currentDirectoryName);
            if (!currentDirectory.exists()) {
                continue;
            }
            if (!currentDirectory.isDirectory()) {
                throw new IOException(currentDirectory.toString() + ": not directory");
            }
            for (int fileIndex = 0; fileIndex < 16; fileIndex++) {
                String currentFileName = fileIndex + ".dat";
                File currentFile = new File(currentDirectory, currentFileName);
                if (!currentFile.exists()) {
                    continue;
                }
                try (RandomAccessFile fileIndexDat = new RandomAccessFile(currentFile, "rw")) {
                    readDatFileFromDisk(fileIndexDat, tableStorage, index, fileIndex);
                } catch (FileNotFoundException e) {
                    continue;
                }
            }
        }
    }

    private static void cleanEmptyDir(File dir) throws IOException {
        if (dir.exists()) {
            if (dir.listFiles().length == 0) {
                if (!dir.delete()) {
                    throw new IOException("File: " + dir.toString() + " can't be deleted");
                }
            } else {
                for (File datFile : dir.listFiles()) {
                    if (!datFile.getName().matches("(0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15)\\.dat")) {
                        throw new IOException(String.format("illegal name of file %s inside dir %s ",
                                datFile.getName(), dir.getName()));
                    } else {
                        if (datFile.length() == 0) {
                            if (!datFile.delete()) {
                                throw new IOException("File: " + datFile.toString() + " can't be deleted");
                            }
                        }
                    }
                }
                if (dir.listFiles().length == 0) {
                    if (!dir.delete()) {
                        throw new IOException("File: " + dir.toString() + " can't be deleted");
                    }
                }
            }
        }
    }

    private static int getIndexOfDir(String key) {
        int hashCode = key.hashCode();
        int indexOfDir = hashCode % 16;
        if (indexOfDir < 0) {
            indexOfDir *= -1;
        }
        return indexOfDir;
    }

    private static int getIndexOfDatFile(String key) {
        int hashCode = key.hashCode();
        int indexOfDat = hashCode / 16 % 16;
        if (indexOfDat < 0) {
            indexOfDat *= -1;
        }
        return indexOfDat;
    }

    private static void parseStorage(HashMap<String, String>[][] parsedStorage, HashMap<String, String> storage,
                                     Set<String> removedKeys) {
        for (String key : removedKeys) {
            int indexOfDir = getIndexOfDir(key);
            int indexOfDat = getIndexOfDatFile(key);
            if (parsedStorage[indexOfDir][indexOfDat] == null) {
                parsedStorage[indexOfDir][indexOfDat] = new HashMap<>();
            }
            parsedStorage[indexOfDir][indexOfDat].put(key, null);
        }
        for (String key : storage.keySet()) {
            int indexOfDir = getIndexOfDir(key);
            int indexOfDat = getIndexOfDatFile(key);
            if (parsedStorage[indexOfDir][indexOfDat] == null) {
                parsedStorage[indexOfDir][indexOfDat] = new HashMap<>();
            }
            parsedStorage[indexOfDir][indexOfDat].put(key, storage.get(key));
        }
    }

    //TODO: проверить!!!
    public static SizeOfTable writeToDatFile(File dir, int dirIndex, File dat, int datIndex,
                                             HashMap<String, String> tableOfChanges) throws IOException {
        int countOfChanges = 0;
        int countOfRemovedKeys = 0;
        int addedKeys = 0;
        if (dat.length() == 0) {
            try (RandomAccessFile datFile = new RandomAccessFile(dat, "rw")) {
                for (String key : tableOfChanges.keySet()) {
                    String value = tableOfChanges.get(key);
                    if (value == null) {
                        continue;
                    }
                    countOfChanges++;
                    addedKeys++;
                    byte[] bytesOfKey = key.getBytes("UTF-8");
                    byte[] bytesOfValue = value.getBytes("UTF-8");
                    datFile.writeInt(bytesOfKey.length);
                    datFile.writeInt(bytesOfValue.length);
                    datFile.write(bytesOfKey);
                    datFile.write(bytesOfValue);
                }
                datFile.close();
            }
        } else {
            File tmpFile = new File(dir, "tmp.dat");
            try (RandomAccessFile oldDatFile = new RandomAccessFile(dat, "rw")) {
                oldDatFile.seek(0);
                try (RandomAccessFile newDatFile = new RandomAccessFile(tmpFile, "rw")) {
                    long length = oldDatFile.length();
                    while (length > 0) {
                        LineOfDB line = readLineOfDatFile(oldDatFile, length, dirIndex, datIndex);
                        length -= line.length;
                        if (tableOfChanges.containsKey(line.key)) {
                            String value = tableOfChanges.get(line.key);
                            tableOfChanges.remove(line.key);
                            countOfChanges++;
                            if (value == null) {
                                countOfRemovedKeys++;
                                continue;
                            }
                            byte[] bytesOfKey = line.key.getBytes("UTF-8");
                            byte[] bytesOfValue = value.getBytes("UTF-8");
                            newDatFile.writeInt(bytesOfKey.length);
                            newDatFile.writeInt(bytesOfValue.length);
                            newDatFile.write(bytesOfKey);
                            newDatFile.write(bytesOfValue);
                        } else {
                            byte[] bytesOfKey = line.key.getBytes("UTF-8");
                            byte[] bytesOfValue = line.value.getBytes("UTF-8");
                            newDatFile.writeInt(bytesOfKey.length);
                            newDatFile.writeInt(bytesOfValue.length);
                            newDatFile.write(bytesOfKey);
                            newDatFile.write(bytesOfValue);
                        }
                    }
                    for (String key : tableOfChanges.keySet()) {
                        String value = tableOfChanges.get(key);
                        if (value == null) {
                            continue;
                        }
                        countOfChanges++;
                        addedKeys++;
                        byte[] bytesOfKey = key.getBytes("UTF-8");
                        byte[] bytesOfValue = value.getBytes("UTF-8");
                        newDatFile.writeInt(bytesOfKey.length);
                        newDatFile.writeInt(bytesOfValue.length);
                        newDatFile.write(bytesOfKey);
                        newDatFile.write(bytesOfValue);
                    }
                    newDatFile.close();
                }
                oldDatFile.close();
            }
            if (!dat.delete()) {
                throw new IOException("Error while commit: file '" + dat.getName() + "' can't be deleted");
            }
            if (!tmpFile.renameTo(dat)) {
                throw new IOException("Error while commit: file '" + tmpFile.getName() + "' can't be renamed");
            }
        }
        return new SizeOfTable(countOfRemovedKeys, addedKeys, countOfChanges);
    }

    //Возвращает число записанных изменений
    public static int writeTableOnDisk(File tableDirectory, HashMap<String, String> tableOfChanges,
                                       Set<String> removedKeys) throws IOException {
        if (tableDirectory == null) {
            throw new IOException("Error! You try to write in null file.");
        }
        HashMap<String, String>[][] parsedStorage = new HashMap[16][16];
        parseStorage(parsedStorage, tableOfChanges, removedKeys);
        SizeOfTable size;
        int countOfChanges = 0;
        int countOfRemovedKeys = 0;
        int countOfAddedKeys = 0;
        for (int indexOfDir = 0; indexOfDir < 16; indexOfDir++) {
            File dir = new File(tableDirectory, indexOfDir + ".dir");
            for (int indexOfDatFile = 0; indexOfDatFile < 16; indexOfDatFile++) {
                File datFile = new File(dir, indexOfDatFile + ".dat");
                if (parsedStorage[indexOfDir][indexOfDatFile] == null) {
                    continue;
                }
                if (!dir.exists()) {
                    if (!dir.mkdir()) {
                        throw new IOException("Directory  " + dir.toString() + " can't be created");
                    }
                }
                if (!datFile.exists()) {
                    if (!datFile.createNewFile()) {
                        throw new IOException("File " + datFile.toString() + " can't be created");
                    }
                }
                size = writeToDatFile(dir, indexOfDir, datFile, indexOfDatFile,
                        parsedStorage[indexOfDir][indexOfDatFile]);
                countOfChanges += size.changes;
                countOfAddedKeys += size.addedKeys;
                countOfRemovedKeys += size.removedKeys;
            }
            cleanEmptyDir(dir);
        }
        //Значит размер изменился и нужно его обновить в size.tsv
        if (countOfAddedKeys - countOfRemovedKeys != 0) {
            int oldSize = readSize(tableDirectory);
            int newSize = oldSize + countOfAddedKeys - countOfRemovedKeys;
            File sizeFile = new File(tableDirectory, "size.tsv");
            writeSizeFile(sizeFile, newSize);
        }
        return countOfChanges;
    }

    public static List<Class<?>> readTableSignature(File tableDirectory) throws IOException {
        if (!tableDirectory.exists()) {
            throw new IllegalArgumentException("not existed table");
        }
        File signature = new File(tableDirectory, "signature.tsv");
        if (!signature.exists()) {
            throw new IOException("signature file not exist: " + signature.getCanonicalPath());
        }

        Scanner scan = new Scanner(signature);
        if (!scan.hasNext()) {
            throw new IOException("empty signature: " + signature.getCanonicalPath());
        }
        String[] types = scan.nextLine().split(" ");
        ColumnTypes ct = new ColumnTypes();
        List<Class<?>> listOfTypes = ct.convertArrayOfStringsToListOfClasses(types);
        scan.close();
        return listOfTypes;
    }

    public static void writeSignature(File tableDirectory, List<String> columnTypes) throws IOException {
        if (!tableDirectory.exists()) {
            throw new IllegalArgumentException("not existed table");
        }
        if (columnTypes == null || columnTypes.size() == 0) {
            throw new IllegalArgumentException("null or empty list of column types");
        }
        File signature = new File(tableDirectory, "signature.tsv");
        if (!signature.createNewFile()) {
            throw new IOException("failed to create new signature.tsv: probably a file with such name already exists");
        }

        try (RandomAccessFile signatureFile = new RandomAccessFile(signature, "rw")) {
            for (int i = 0; i < columnTypes.size(); i++) {
                String type = columnTypes.get(i);
                if (type.matches("int|byte|long|float|double|boolean|String")) {
                    signatureFile.write(type.getBytes(StandardCharsets.UTF_8));
                    if (i != (columnTypes.size() - 1)) {
                        signatureFile.write(' ');
                    }
                } else {
                    signatureFile.close();
                    signature.delete();
                    throw new IOException("writing signature: illegal type: " + type);
                }
            }
        }
    }

    //Если в таблице не было файла size.tsv - дописывает его уже с правильным числом внутри
    //Возвращает реальный размер таблицы
    public static int checkTable(File tableDir) throws IOException {
        if (!tableDir.exists()) {
            throw new IOException(String.format("DBTable: table dir %s does not exist", tableDir));
        }
        File[] listFiles = tableDir.listFiles();
        if (listFiles == null) {
            throw new IOException(String.format("DBTable: file %s is not a dir", tableDir));
        }
        if (listFiles.length == 0) {
            throw new IOException("empty dir");
        }
        int realSize = 0;
        boolean isSizeFile = false;
        for (File dirFile : listFiles) {
            if (dirFile.isDirectory()) {
                if (!dirFile.getName().matches("(0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15)\\.dir")) {
                    throw new IOException(String.format("illegal name of dir %s inside table %s",
                            dirFile.getName(), tableDir.getName()));
                } else {
                    File[] listFilesInsideDir = dirFile.listFiles();
                    if (listFilesInsideDir.length == 0) {
                        throw new IOException("empty dir " + dirFile.getName());
                    }
                    for (File datFiles : listFilesInsideDir) {
                        if (!datFiles.getName().matches("(0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15)\\.dat")) {
                            throw new IOException(String.format("illegal name of file %s inside dir %s inside table %s",
                                    datFiles.getName(), dirFile.getName(), tableDir.getName()));
                        } else {
                            if (datFiles.length() == 0) {
                                throw new IOException("empty file " + datFiles.getName());
                            }
                            realSize += checkKeysInDatFileOnRightHashCode(dirFile, datFiles);
                        }
                    }
                }
            } else {
                if (!dirFile.getName().equals("signature.tsv") && !dirFile.getName().equals("size.tsv")) {
                    throw new IOException("illegal file " + dirFile.getName());
                }
                if (dirFile.getName().equals("size.tsv")) {
                    isSizeFile = true;
                }
            }
        }
        if (!isSizeFile) {
            File sizeFile = new File(tableDir, "size.tsv");
            if (!sizeFile.createNewFile()) {
                throw new IOException("failed to create new size.tsv: probably a file with such name already exists");
            }
            writeSizeFile(sizeFile, realSize);
        } else {
            checkTableSize(tableDir, realSize);
        }
        return realSize;
    }

    public static void writeSizeFile(File sizeFile, int size) throws IOException {
        try (RandomAccessFile sizeRAFile = new RandomAccessFile(sizeFile, "rw")) {
            sizeRAFile.setLength(0);
            sizeRAFile.seek(0);
            sizeRAFile.write(Integer.toString(size).getBytes(StandardCharsets.UTF_8));
        }
    }

    public static int readSize(File tableDirectory) throws IOException {
        File sizeFile = new File(tableDirectory, "size.tsv");
        if (!sizeFile.exists()) {
            throw new IOException("no file size.tsv");
        }
        int size = -1;
        try (RandomAccessFile sizeRAFile = new RandomAccessFile(sizeFile, "r")) {
            if (sizeRAFile.length() == 0) {
                throw new IOException("empty file size.tsv");
            }
            byte[] bytes = new byte[64];
            sizeRAFile.read(bytes);
            String sizeAsString = new String(bytes, "UTF-8");
            sizeAsString = sizeAsString.trim();
            size = Integer.parseInt(sizeAsString);
        }
        return size;
    }

    //TODO: проверить
    private static void checkTableSize(File tableDirectory, int realSize) throws IOException {
        File sizeFile = new File(tableDirectory, "size.tsv");
        if (!sizeFile.exists()) {
            if (!sizeFile.createNewFile()) {
                throw new IOException("failed to create new size.tsv");
            }
            writeSizeFile(sizeFile, realSize);
        } else {
            boolean needRewriteSizeFile = false;
            try (RandomAccessFile sizeRAFile = new RandomAccessFile(sizeFile, "r")) {
                if (sizeRAFile.length() == 0) {
                    needRewriteSizeFile = true;
                }
                byte[] bytes = new byte[64];
                sizeRAFile.read(bytes);
                String sizeAsString = new String(bytes, "UTF-8");
                sizeAsString = sizeAsString.trim();
                int sizeInFile = Integer.parseInt(sizeAsString);
                if (sizeInFile != realSize) {
                    needRewriteSizeFile = true;
                }
            }
            if (needRewriteSizeFile) {
                writeSizeFile(sizeFile, realSize);
            }
        }
    }

    //TODO: проверить это
    //Возвращает количество ключей в dat файле
    private static int checkKeysInDatFileOnRightHashCode(File dirFile, File datFile) throws IOException {
        String dirIndex = dirFile.getName();
        int last = dirIndex.lastIndexOf('.');
        dirIndex = dirIndex.substring(0, last);
        String datIndex = datFile.getName();
        last = datIndex.lastIndexOf('.');
        datIndex = datIndex.substring(0, last);
        RandomAccessFile fileIndexDat = new RandomAccessFile(datFile, "rw");
        long length = datFile.length();
        int countOfKeys = 0;
        fileIndexDat.seek(0);
        while (length > 0) {
            //Внутри readLineOfDatFile вызывается метод checkKeyOnRightHashCode
            LineOfDB line = readLineOfDatFile(fileIndexDat, length,
                    Integer.parseInt(dirIndex), Integer.parseInt(datIndex));
            length -= line.length;
            countOfKeys++;
        }
        return countOfKeys;
    }

    public static String loadValueByKey(String key, File tableDir) throws IOException {
        int indexDir = getIndexOfDir(key);
        int indexDat = getIndexOfDatFile(key);
        File dir = new File(tableDir, Integer.toString(indexDir) + ".dir");
        if (!dir.exists()) {
            return null;
        }
        File dat = new File(dir, Integer.toString(indexDat) + ".dat");
        try (RandomAccessFile datFile = new RandomAccessFile(dat, "rw")) {
            long length;
            length = datFile.length();
            if (length == 0) {
                return null;
            }
            datFile.seek(0);
            while (length > 0) {
                LineOfDB line = readLineOfDatFile(datFile, length, indexDir, indexDat);
                length -= line.length;
                if (line.key.equals(key)) {
                    return line.value;
                }
            }
            datFile.close();
        }
        return null;
    }
}
