package ru.fizteh.fivt.students.dmitryKonturov.dataBase.databaseImplementation;

import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;
import ru.fizteh.fivt.students.dmitryKonturov.dataBase.DatabaseException;
import ru.fizteh.fivt.students.dmitryKonturov.dataBase.utils.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TableImplementation implements Table {
    private final String tableName;
    private final TableProviderImplementation tableProvider;
    private final List<Class<?>> columnTypes;
    private final int columnsCount;

    private HashMap<String, Storeable> savedMap;
    private final Path tablePath;

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);
    private final Lock readLock  = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();
    private final int localTransactionId; // for library version

    private ThreadLocal<Map<String, Storeable>> currentChangesMapDiff = new ThreadLocal<Map<String, Storeable>>() {
        @Override
        protected Map<String, Storeable> initialValue() {
            return new HashMap<>();
        }
    };

    /**
     * loads database from its folder
     */
    TableImplementation(String tableName, TableProviderImplementation tableProvider, int transactionId)
            throws IOException, DatabaseException {

        this.tableName = tableName;
        this.tableProvider = tableProvider;
        this.localTransactionId = transactionId;
        this.tablePath = tableProvider.getWorkspace().resolve(tableName);
        Path signatureFile = tablePath.resolve(StoreableUtils.getSignatureFileName());
        this.columnTypes = StoreableUtils.loadSignatureFile(signatureFile);
        this.columnsCount = columnTypes.size();
        this.savedMap = new HashMap<>();
        Map<String, String> tmpBase = new HashMap<>();
        MultiFileMapLoaderWriter.loadDatabase(tableProvider.getWorkspace(), tableName, tmpBase);
        for (Map.Entry<String, String> entry : tmpBase.entrySet()) {
            try {
                Storeable storeable = tableProvider.deserialize(this, entry.getValue());
                savedMap.put(entry.getKey(), storeable);
            } catch (Exception e) {
                throw new DatabaseException("Cannot deserialize file", e);
            }

        }

    }

    /**
     * not even try to load database
     */
    TableImplementation(String tableName, TableProviderImplementation tableProvider, List<Class<?>> columnTypes,
                        int transactionId) throws
            IOException, RuntimeException, DatabaseException {

        this.tableName = tableName;
        this.tableProvider = tableProvider;
        this.localTransactionId = transactionId;
        this.columnTypes = new ArrayList<>();
        for (Class<?> type : columnTypes) {
            if (type == null) {
                throw new IllegalArgumentException("type can't be null");
            }
            if (!StoreableUtils.isSupportedType(type)) {
                throw new IllegalArgumentException("type no supported");
            }
            this.columnTypes.add(type);
        }
        this.columnsCount = columnTypes.size();
        this.savedMap = new HashMap<>();
        this.tablePath = tableProvider.getWorkspace().resolve(tableName);
        Files.createDirectory(tablePath);
        StoreableUtils.writeSignatureFile(tablePath.resolve(StoreableUtils.getSignatureFileName()),
                                          this.columnTypes);

    }

    private boolean isTableStoreableEqual(Storeable first, Storeable second) {
        if (first == null || second == null) {
            return first == null && second == null;
        }
        boolean result = true;
        for (int i = 0; i < columnsCount; ++i) {
            Object firstObject = first.getColumnAt(i);
            Object secondObject = second.getColumnAt(i);
            if (firstObject == null) {
                result &= (secondObject == null);
            } else {
                result &= secondObject != null && firstObject.equals(secondObject);
            }
        }
        return result;
    }

    private Map<String, Storeable> getCurrentChanges(int transactionId) {
        if (transactionId < 0) {
            return currentChangesMapDiff.get();
        } else {
            return tableProvider.getTransactionPool().getDiffTable(transactionId);
        }
    }

    public int getUnsavedChangesCount(int transactionId) { //need external sync
        int changesNum = 0;
        Map<String, Storeable> currentChangesMap = getCurrentChanges(transactionId);
        for (Map.Entry<String, Storeable> entry : currentChangesMap.entrySet()) {
            String key = entry.getKey();
            Storeable value = entry.getValue();
            Storeable savedValue = savedMap.get(key);

            if (value == null) {
                if (savedValue != null) {
                    ++changesNum;
                }
            } else {
                if (savedValue == null) {
                    ++changesNum;
                } else if (!isTableStoreableEqual(value, savedValue)) { // must be true
                    ++changesNum;
                }
            }

        }
        return changesNum;
    }

    private void checkTableState() {
        if (!tableProvider.isProviderLoading() && tableProvider.getTable(tableName) != this) {
            throw new IllegalStateException("Table was removed");
        }
    }

    private void checkKey(String key) throws IllegalArgumentException {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }
        if (key.trim().isEmpty()) {
            throw new IllegalArgumentException("key must have positive length");
        }
        if (key.matches("(.*\\s+.*)+")) {
            throw new IllegalArgumentException("key contains space characters");
        }
    }

    public int getLocalTransactionId() {
        return localTransactionId;
    }

    @Override
    public String getName() {
        checkTableState();
        return tableName;
    }

    @Override
    public Storeable get(String key) {
        return get(key, localTransactionId);
    }

    public Storeable get(String key, int transactionId) throws IllegalArgumentException {
        if (key == null) {
            throw new IllegalArgumentException("Empty key");
        }
        checkTableState();
        Map<String, Storeable> currentChangesMap = getCurrentChanges(transactionId);
        //todo synctonize currentChanges
        if (currentChangesMap.containsKey(key)) {
            return currentChangesMap.get(key);
        } else {
            readLock.lock();
            try {
                return savedMap.get(key);
            } finally {
                readLock.unlock();
            }
        }

    }

    @Override
    public Storeable put(String key, Storeable value) {
        return put(key, value, localTransactionId);
    }

    public Storeable put(String key, Storeable value, int transactionId) throws IllegalArgumentException {
        checkKey(key);
        if (value == null) {
            throw new IllegalArgumentException("Empty value");
        }
        checkTableState();
        StoreableUtils.checkStoreableBelongsToTable(this, value);

        //todo sync changes
        Map<String, Storeable> currentChangesMap = getCurrentChanges(transactionId);

        Storeable toReturn = get(key, transactionId);
        currentChangesMap.put(key, value);
        return toReturn;

    }

    @Override
    public Storeable remove(String key) {
        return remove(key, localTransactionId);
    }

    public Storeable remove(String key, int transactionId) throws IllegalArgumentException {
        if (key == null) {
            throw new IllegalArgumentException("Empty key");
        }
        checkTableState();

        //todo sync changes
        Map<String, Storeable> currentChangesMap = getCurrentChanges(transactionId);


        Storeable toReturn = get(key, transactionId);
        currentChangesMap.put(key, null);
        return toReturn;
    }

    @Override
    public int size() {
        return size(localTransactionId);
    }

    public int size(int transactionId) {
        checkTableState();
        readLock.lock();
        try {
            int tableSize = savedMap.size();

            //todo sync changes
            Map<String, Storeable> currentChangesMap = getCurrentChanges(transactionId);

            for (Map.Entry<String, Storeable> entry : currentChangesMap.entrySet()) {
                String key = entry.getKey();
                Storeable value = entry.getValue();
                Storeable savedValue = savedMap.get(key);
                if (savedValue == null) { // Was not saved
                    if (value != null) {
                        ++tableSize;
                    }
                } else {                  // saved
                    if (value == null) {
                        --tableSize;
                    }
                }
            }
            return tableSize;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public int commit() throws IOException {
        return commit(localTransactionId, false);
    }

    public int commit(int transactionId, boolean needToFinishTransaction) throws IOException {
        checkTableState();
        writeLock.lock();
        try {
            int changesNumber = 0;
            boolean[] changedTableHash = new boolean[16];
            //todo sync changes
            Map<String, Storeable> currentChangesMap = getCurrentChanges(transactionId);

            for (Map.Entry<String, Storeable> entry : currentChangesMap.entrySet()) {
                String key = entry.getKey();
                int keyHash = Math.abs(key.hashCode());
                Storeable value = entry.getValue();
                Storeable savedValue;
                if (value == null) { // need to remove value
                    savedValue = savedMap.remove(key);
                    changedTableHash[keyHash % 16] = true;
                    if (savedValue != null) { //if contains before
                        ++changesNumber;
                    }
                } else {
                    savedValue = savedMap.put(key, value);
                    changedTableHash[keyHash % 16] = true;
                    if (savedValue == null) { // new key mapping
                        ++changesNumber;
                    } else {                  // only if different values
                        if (!isTableStoreableEqual(savedValue, value)) { //must be false
                            ++changesNumber;
                        }
                    }
                }
            }

            currentChangesMap.clear();
            if (needToFinishTransaction && transactionId >= 0) {
                tableProvider.getTransactionPool().deleteTransaction(transactionId);
            }

            Map<String, String>[] savedStringMap = new HashMap[16];
            for (int i = 0; i < 16; ++i) {
                if (changedTableHash[i]) {
                    savedStringMap[i] = new HashMap<>();
                }
            }
            for (Map.Entry<String, Storeable> entry : savedMap.entrySet()) {
                try {
                    String key = entry.getKey();
                    int keyHash = Math.abs(key.hashCode());
                    if (changedTableHash[keyHash % 16]) {
                        savedStringMap[keyHash % 16].put(key, tableProvider.serialize(this, entry.getValue()));
                    }
                } catch (Exception e) {
                    throw new IOException("Cannot serialize storable");
                }
            }
            try {
                MultiFileMapLoaderWriter.writeMultipleDatabase(tableProvider.getWorkspace(), tableName, savedStringMap);
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw new IOException(e);
            }
            return changesNumber;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public int rollback() {
        return rollback(localTransactionId, false);
    }

    public int rollback(int transactionId, boolean needToFinishTransaction) {
        checkTableState();
        int toReturn;
        //todo sync changes
        Map<String, Storeable> currentChangesMap = getCurrentChanges(transactionId);

        readLock.lock();
        try {
            toReturn = getUnsavedChangesCount(transactionId);
        } finally {
            readLock.unlock();
        }
        currentChangesMap.clear();
        if (needToFinishTransaction && transactionId >= 0) {
            tableProvider.getTransactionPool().deleteTransaction(transactionId);
        }
        return toReturn;
    }

    @Override
    public int getColumnsCount() {
        checkTableState();
        return columnsCount;
    }

    @Override
    public Class<?> getColumnType(int columnIndex) throws IndexOutOfBoundsException {
        checkTableState();
        if (columnIndex < 0) {
            throw new IndexOutOfBoundsException("Negative index");
        }
        if (columnIndex >= columnsCount) {
            throw new IndexOutOfBoundsException("Index bigger than columns count");
        }
        return columnTypes.get(columnIndex);

    }
}
