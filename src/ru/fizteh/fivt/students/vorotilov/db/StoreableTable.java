package ru.fizteh.fivt.students.vorotilov.db;

import ru.fizteh.fivt.storage.structured.*;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

/**
 * Представляет интерфейс для работы с таблицей, содержащей ключи-значения. Ключи должны быть уникальными.
 *
 * Транзакционность: изменения фиксируются или откатываются с помощью методов {@link #commit()} или {@link #rollback()},
 * соответственно. Предполагается, что между вызовами этих методов никаких операций ввода-вывода не происходит.
 *
 * Данный интерфейс не является потокобезопасным.
 */
public class StoreableTable implements Table {

    private TableProvider tableProvider;
    private List<Class<?>> columnTypes;
    private final File tableRootDir;
    private TableFile[][] tableFiles = new TableFile[16][16];
    private boolean[][] tableFileModified = new boolean[16][16];
    private HashMap<String, Storeable> tableOnDisk;
    private HashMap<String, Storeable> tableIndexedData = new HashMap<>();

    private void index() {
        File[] subDirsList = tableRootDir.listFiles();
        if (subDirsList != null) {
            for (File subDir: subDirsList) {
                int numberOfSubDir;
                if (subDir.getName().equals(SignatureFile.signatureFileName)) {
                    continue;
                }
                if (!subDir.isDirectory()) {
                    throw new IllegalStateException("In table root dir found object is not a directory");
                }
                String[] tableSubDirName = subDir.getName().split("[.]");
                try {
                    numberOfSubDir = Integer.parseInt(tableSubDirName[0]);
                } catch (NumberFormatException e) {
                    throw new IllegalStateException("Table root directory contains not 0.dir ... 15.dir");
                }
                if (numberOfSubDir < 0 || numberOfSubDir > 15
                        || !tableSubDirName[1].equals("dir") || tableSubDirName.length != 2) {
                    throw new IllegalStateException("Table root directory contains not 0.dir ... 15.dir");
                }
                File[] subFilesList = subDir.listFiles();
                if (subFilesList != null && subFilesList.length == 0) {
                    throw new IllegalStateException("data base contains empty dir");
                }
                if (subFilesList != null) {
                    for (File subFile: subFilesList) {
                        int numberOfSubFile;
                        if (!subFile.isFile()) {
                            throw new IllegalStateException("In table sub dir found object is not a file");
                        }
                        String[] dbFileName = subFile.getName().split("[.]");
                        try {
                            numberOfSubFile = Integer.parseInt(dbFileName[0]);
                        } catch (NumberFormatException e) {
                            throw new IllegalStateException("Table sub directory contains not 0.dat ... 15.dat");
                        }
                        if (numberOfSubFile < 0 || numberOfSubFile > 15
                                || !dbFileName[1].equals("dat") || dbFileName.length != 2) {
                            throw new IllegalStateException("Table sub directory contains not 0.dat ... 15.dat");
                        } else if (subFile.length() == 0) {
                            throw new IllegalStateException("Empty file in sub dir");
                        } else {
                            tableFiles[numberOfSubDir][numberOfSubFile] = new TableFile(subFile);
                            List<TableFile.Entry> fileData = tableFiles[numberOfSubDir][numberOfSubFile].readEntries();
                            for (TableFile.Entry i : fileData) {
                                HashcodeDestination dest = new HashcodeDestination(i.getKey());
                                if (dest.getFile() != numberOfSubFile || dest.getDir() != numberOfSubDir) {
                                    throw new IllegalStateException("Wrong key placement");
                                }
                                try {
                                    tableIndexedData.put(i.getKey(),
                                            tableProvider.deserialize(this, i.getValue()));
                                } catch (ParseException e) {
                                    throw new IllegalStateException("Can't deserialize", e);
                                }
                            }
                        }
                    }
                }
            }
        }
        tableOnDisk = new HashMap<>(tableIndexedData);
    }

    StoreableTable(TableProvider tableProvider, File tableRootDir, List<Class<?>> classes) {
        if (tableRootDir == null) {
            throw new IllegalArgumentException("Table root dir is null");
        } else if (!tableRootDir.exists()) {
            throw new IllegalArgumentException("Proposed root dir not exists");
        } else if (!tableRootDir.isDirectory()) {
            throw new IllegalArgumentException("Proposed object is not directory");
        }
        if (classes == null) {
            throw new IllegalArgumentException("Column type is null");
        }
        this.tableProvider = tableProvider;
        this.tableRootDir = tableRootDir;
        columnTypes = classes;
        try {
            SignatureFile.createSignature(tableRootDir, columnTypes);
        } catch (IOException e) {
            throw new IllegalStateException("Can't write signature", e);
        }
        index();
    }

    StoreableTable(TableProvider tableProvider, File tableRootDir) {
        if (tableRootDir == null) {
            throw new IllegalArgumentException("Table root dir is null");
        } else if (!tableRootDir.exists()) {
            throw new IllegalArgumentException("Proposed root dir not exists");
        } else if (!tableRootDir.isDirectory()) {
            throw new IllegalArgumentException("Proposed object is not directory");
        }
        this.tableProvider = tableProvider;
        this.tableRootDir = tableRootDir;

        try {
            columnTypes = SignatureFile.readSignature(tableRootDir);
        } catch (IOException e) {
            throw new IllegalStateException("Can't read signature", e);
        }
        index();
    }

    /**
     * Возвращает название таблицы.
     *
     * @return Название таблицы.
     */
    @Override
    public String getName() {
        return tableRootDir.getName();
    }

    /**
     * Получает значение по указанному ключу.
     *
     * @param key Ключ для поиска значения. Не может быть null.
     * @return Значение. Если не найдено, возвращает null.
     *
     * @throws IllegalArgumentException Если значение параметра key является null.
     */
    @Override
    public Storeable get(String key) {
        checkKey(key);
        return tableIndexedData.get(key);
    }

    /**
     * Устанавливает значение по указанному ключу.
     *
     * @param key Ключ для нового значения. Не может быть null.
     * @param value Новое значение. Не может быть null.
     * @return Значение, которое было записано по этому ключу ранее. Если ранее значения не было записано,
     * возвращает null.
     *
     * @throws IllegalArgumentException Если значение параметров key или value является null.
     * @throws ru.fizteh.fivt.storage.structured.ColumnFormatException - при попытке передать Storeable с колонками другого типа.
     */
    @Override
    public Storeable put(String key, Storeable value) throws ColumnFormatException {
        checkKey(key);
        checkValue(value);
        Storeable oldValue = tableIndexedData.get(key);
        if (oldValue == null || !oldValue.equals(value)) {
            HashcodeDestination dest = new HashcodeDestination(key);
            tableFileModified[dest.getDir()][dest.getFile()] = true;
            tableIndexedData.put(key, value);
        }
        return oldValue;
    }

    /**
     * Удаляет значение по указанному ключу.
     *
     * @param key Ключ для поиска значения. Не может быть null.
     * @return Предыдущее значение. Если не найдено, возвращает null.
     *
     * @throws IllegalArgumentException Если значение параметра key является null.
     */
    @Override
    public Storeable remove(String key) {
        checkKey(key);
        Storeable oldValue = tableIndexedData.remove(key);
        if (oldValue != null) {
            HashcodeDestination dest = new HashcodeDestination(key);
            tableFileModified[dest.getDir()][dest.getFile()] = true;
        }
        return oldValue;
    }

    /**
     * Возвращает количество ключей в таблице. Возвращает размер текущей версии, с учётом незафиксированных изменений.
     *
     * @return Количество ключей в таблице.
     */
    @Override
    public int size() {
        return tableIndexedData.size();
    }

    /**
     * Выполняет фиксацию изменений.
     *
     * @return Число записанных изменений.
     *
     * @throws java.io.IOException если произошла ошибка ввода/вывода. Целостность таблицы не гарантируется.
     */
    @Override
    public int commit() throws IOException {
        int numberOfCommittedChanges = uncommittedChanges();
        Set<Map.Entry<String, Storeable>> dbSet = tableIndexedData.entrySet();
        for (int nDir = 0; nDir < 16; ++nDir) {
            for (int nFile = 0; nFile < 16; ++nFile) {
                if (tableFileModified[nDir][nFile]) {
                    if (tableFiles[nDir][nFile] == null) {
                        File subDir = new File(tableRootDir, Integer.toString(nDir) + ".dir");
                        File subFile = new File(subDir, Integer.toString(nFile) + ".dat");
                        if (!subDir.exists()) {
                            if (!subDir.mkdir()) {
                                throw new IllegalStateException("Sub dir was not created");
                            }
                        }
                        tableFiles[nDir][nFile] = new TableFile(subFile);
                    }
                    List<TableFile.Entry> fileData = new ArrayList<>();
                    Iterator<Map.Entry<String, Storeable>> iter = dbSet.iterator();
                    while (iter.hasNext()) {
                        Map.Entry<String, Storeable> tempMapEntry = iter.next();
                        HashcodeDestination dest = new HashcodeDestination(tempMapEntry.getKey());
                        if (dest.getDir() == nDir && dest.getFile() == nFile) {
                            fileData.add(new TableFile.Entry(tempMapEntry.getKey(),
                                    tableProvider.serialize(this, tempMapEntry.getValue())));
                        }
                    }
                    tableFiles[nDir][nFile].writeEntries(fileData);
                    tableFileModified[nDir][nFile] = false;
                }
            }
        }
        tableOnDisk.clear();
        tableOnDisk.putAll(tableIndexedData);
        return numberOfCommittedChanges;
    }

    /**
     * Выполняет откат изменений с момента последней фиксации.
     *
     * @return Число откаченных изменений.
     */
    @Override
    public int rollback() {
        int numberOfRolledChanges = uncommittedChanges();
        tableIndexedData.clear();
        tableIndexedData.putAll(tableOnDisk);
        for (int nDir = 0; nDir < 16; ++nDir) {
            for (int nFile = 0; nFile < 16; ++nFile) {
                tableFileModified[nDir][nFile] = false;
            }
        }
        return numberOfRolledChanges;
    }

    public void close() throws IOException {
        File[] listOfSubDirs = tableRootDir.listFiles();
        if (listOfSubDirs != null) {
            for (File subDir : listOfSubDirs) {
                if (subDir.exists()) {
                    File[] listOfFiles = subDir.listFiles();
                    if (listOfFiles != null && listOfFiles.length == 0) {
                        if (!subDir.delete()) {
                            throw new IllegalStateException("Can't delete empty sub dir");
                        }
                    }
                }
            }
        }
    }

    public int uncommittedChanges() {
        Set<Map.Entry<String, Storeable>> indexedSet = tableIndexedData.entrySet();
        Set<Map.Entry<String, Storeable>> diskSet = tableOnDisk.entrySet();
        int count = 0;
        Iterator<Map.Entry<String, Storeable>> iter1 = indexedSet.iterator();
        Iterator<Map.Entry<String, Storeable>> iter2 = diskSet.iterator();
        while (iter1.hasNext()) {
            Map.Entry<String, Storeable> next = iter1.next();
            Storeable entryOnDisk = tableOnDisk.get(next.getKey());
            if (entryOnDisk == null) {
                //записи на диске нет, то она изменение
                ++count;
            } else if (!entryOnDisk.equals(next.getValue())) {
                //запись на диске есть, но она другая, тоже изменение
                ++count;
            }
        }
        while (iter2.hasNext()) {
            Map.Entry<String, Storeable> next = iter2.next();
            Storeable entryIndexed = tableIndexedData.get(next.getKey());
            if (entryIndexed == null) {
                //на диске есть, индексированной нет, изменение
                ++count;
            }
        }
        return count;
    }

    /**
     * Возвращает количество колонок в таблице.
     *
     * @return Количество колонок в таблице.
     */
    @Override
    public int getColumnsCount() {
        return columnTypes.size();
    }

    /**
     * Возвращает тип значений в колонке.
     *
     * @param columnIndex Индекс колонки. Начинается с нуля.
     * @return Класс, представляющий тип значения.
     *
     * @throws IndexOutOfBoundsException - неверный индекс колонки
     */
    @Override
    public Class<?> getColumnType(int columnIndex) throws IndexOutOfBoundsException {
        if (columnIndex < 0 || columnIndex >= columnTypes.size()) {
            throw new IndexOutOfBoundsException();
        }
        return columnTypes.get(columnIndex);
    }

    public List<Class<?>> getColumnTypes() {
        return columnTypes;
    }

    private void checkKey(String key) throws IllegalArgumentException {
        if (key == null) {
            throw new IllegalArgumentException("Key is null");
        }
        if (key.equals("")) {
            throw new IllegalArgumentException("Key is empty");
        }
        if (key.contains(" ") || key.contains("\t") || key.contains("\n")
                || key.contains("\\x0B") || key.contains("\f") || key.contains("\r")) {
            throw new IllegalArgumentException("Kay contains whitespaces");
        }
    }

    private void checkValue(Storeable value) throws ColumnFormatException {
        if (value == null) {
            throw new IllegalArgumentException("Value is null");
        }
        try {
            for (int i = 0; i < columnTypes.size(); ++i) {
                if (value.getColumnAt(i) != null && !columnTypes.get(i).equals(value.getColumnAt(i).getClass())) {
                    throw new ColumnFormatException("Wrong column type. was: "
                            + value.getColumnAt(i).getClass().toString() + "; expected: " + columnTypes.get(i));
                }
            }
            boolean unusedValue = true;
            try {
                value.getColumnAt(columnTypes.size());
            } catch (IndexOutOfBoundsException e) {
                unusedValue = false;
            }
            if (unusedValue) {
                throw new ColumnFormatException("Alien value");
            }
        } catch (IndexOutOfBoundsException e) {
            throw new ColumnFormatException("Alien value", e);
        }

    }

}
