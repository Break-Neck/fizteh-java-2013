package ru.fizteh.fivt.students.dzvonarev.filemap;

import ru.fizteh.fivt.storage.structured.Storeable;

import java.util.HashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TransactionChanges {

    private HashMap<Long, HashMap<String, Storeable>> transactionToChangesMap = new HashMap<>();
    private long countOfTrans = 0;
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private static TransactionChanges it = new TransactionChanges();

    public TransactionChanges() {
    }

    public static TransactionChanges getInstance() {
        return it;
    }

    public HashMap<String, Storeable> getChangesMap(Long number) {
        lock.readLock().lock();
        try {
            return transactionToChangesMap.get(number);
        } finally {
            lock.readLock().unlock();
        }
    }

    public long createTransaction() {
        lock.writeLock().lock();
        try {
            ++countOfTrans;
            transactionToChangesMap.put(countOfTrans, new HashMap<String, Storeable>());
            return countOfTrans;
        } finally {
            lock.writeLock().unlock();
        }
    }

}
