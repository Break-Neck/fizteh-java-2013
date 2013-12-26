package ru.fizteh.fivt.students.dmitryKonturov.dataBase.databaseImplementation;

import ru.fizteh.fivt.storage.structured.Storeable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TransactionPool {
    private Map<Integer, String> transactionTableName = new HashMap<>();
    private Map<Integer, Map<String, Storeable>> transactionDiff = new HashMap<>();
    private int lastId = 0;
    private static final int MAX_TRANSACTION_ID = 100000; // id in range [0, MaxTransaction)

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);
    private final Lock writeLock = readWriteLock.writeLock();
    private final Lock readLock = readWriteLock.readLock();


    public static int getMaxTransactionId() {
        return MAX_TRANSACTION_ID;
    }

    public Map<String, Storeable> getDiffTable(int transactionId) {
        readLock.lock();
        try {
            return transactionDiff.get(transactionId);
        } finally {
            readLock.unlock();
        }
    }

    public String getTableName(int transactionId) {
        readLock.lock();
        try {
            return transactionTableName.get(transactionId);
        } finally {
            readLock.unlock();
        }
    }

    public boolean transactionExists(int transactionId) {
        readLock.lock();
        try {
            return transactionDiff.containsKey(transactionId);
        } finally {
            readLock.unlock();
        }
    }

    public int createTransaction(String tableName) {
        writeLock.lock();
        int prevLast =  lastId;
        try {
            while (transactionDiff.containsKey(lastId)) {
                lastId = (lastId + 1) % MAX_TRANSACTION_ID;
                if (lastId == prevLast) {
                    throw new RuntimeException("All transactions are used");
                }
            }
            transactionDiff.put(lastId, new HashMap<String, Storeable>());
            transactionTableName.put(lastId, tableName);
        } finally {
            writeLock.unlock();
        }
        return  lastId;
    }

    public void deleteTransaction(int transactionId) {
        writeLock.lock();
        try {
            transactionDiff.remove(transactionId);
            transactionTableName.remove(transactionId);
        } finally {
            writeLock.unlock();
        }
    }
}
