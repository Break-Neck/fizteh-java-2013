package ru.fizteh.fivt.students.dzvonarev.filemap;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TransactionManager {

    private Map<String, Transaction> changesMap;
    private MyTableProvider currentProvider;  // for starting transaction
    private int numberOfTransactions;
    private ReadWriteLock lock = new ReentrantReadWriteLock();

    public TransactionManager(MyTableProvider provider) {
        currentProvider = provider;
        changesMap = new HashMap<>();
        numberOfTransactions = 0;
    }

    public String getTransactionId() {
        StringBuilder builder = new StringBuilder();
        lock.writeLock().lock();
        try {
            String numTrans;
            if (numberOfTransactions == 99999) {
                numTrans = "00000";
            } else {
                numTrans = (new Integer(numberOfTransactions)).toString();
            }
            int transLen = numTrans.length();
            while (transLen < 5) {
                builder.append(0);
                ++transLen;
            }
            builder.append(numTrans);
            ++numberOfTransactions;
        } finally {
            lock.writeLock().unlock();
        }
        return builder.toString();
    }

    public String startTransaction(String name) {
        Transaction transaction = new Transaction(currentProvider, name, this);
        lock.writeLock().lock();
        try {
            changesMap.put(transaction.getId(), transaction);
        } finally {
            lock.writeLock().unlock();
        }
        return transaction.getId();
    }

    void stopTransaction(String id) {
        lock.writeLock().lock();
        try {
            changesMap.remove(id);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Transaction getTransaction(String id) {
        lock.readLock().lock();
        try {
            return changesMap.get(id);
        } finally {
            lock.readLock().unlock();
        }
    }

}
