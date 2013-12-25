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
    private final Lock readLock  = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();

    Map<String, Storeable> getDiffTable(int transactionId) {

        return transactionDiff.get(transactionId);
    }

    String getTableName(int transactionId) {
        return transactionTableName.get(transactionId);
    }

    boolean transactionExists(int transactionId) {
        return transactionDiff.containsKey(transactionId);
    }

    int createTransaction(String tableName) {
        int prevLast =  lastId;
        while (transactionDiff.containsKey(lastId)) {
            lastId = (lastId + 1) % MAX_TRANSACTION_ID;
            if (lastId == prevLast) {
                throw new RuntimeException("All transactions are used");
            }
        }
        transactionDiff.put(lastId, new HashMap<String, Storeable>());
        transactionTableName.put(lastId, tableName);
        return  lastId;
    }

    void deleteTransaction(int transactionId) {
        transactionDiff.remove(transactionId);
        transactionTableName.remove(transactionId);
    }
}
