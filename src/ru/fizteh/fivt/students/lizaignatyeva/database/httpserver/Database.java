package ru.fizteh.fivt.students.lizaignatyeva.database.httpserver;

import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.students.lizaignatyeva.database.MyTable;
import ru.fizteh.fivt.students.lizaignatyeva.database.MyTableProvider;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Database {
    public static class Transaction {
        public MyTable table;
        public HashMap<String, Storeable> localChanges;
        public final ReentrantLock lock;

        private HashMap<String, Storeable> oldChanges;

        public Transaction(MyTable table, HashMap<String, Storeable> localChanges, ReentrantLock lock) {
            this.table = table;
            this.localChanges = localChanges;
            this.lock = lock;
        }

        public void start() {
            lock.lock();
            oldChanges = table.getLocalChanges();
            table.setLocalChanges(localChanges);
        }

        public void end() {
            table.setLocalChanges(oldChanges);
            lock.unlock();
        }

    }

    public static boolean isValidTransactionName(String name) {
        return name != null && name.matches("\\d{5,5}");
    }

    public MyTableProvider tableProvider;
    public HashMap<String, MyTable> transactions;
    private final ReentrantReadWriteLock transactionLock;

    public HashMap<String, HashMap<String, Storeable>> transactionsChanges;
    public HashMap<String, ReentrantLock> transactionLocks;

    private static final int MAX_TRANSACTION_ID = 100000;

    public Database(MyTableProvider tableProvider) {
        this.tableProvider = tableProvider;
        this.transactions = new HashMap<>();
        this.transactionLock = new ReentrantReadWriteLock();
        this.transactionLocks = new HashMap<>();
        this.transactionsChanges = new HashMap<>();
    }

    public Transaction getTransaction(String transactionName) {
        transactionLock.readLock().lock();
        try {
            if (transactions.containsKey(transactionName)) {
                return new Transaction(
                    transactions.get(transactionName),
                    transactionsChanges.get(transactionName),
                    transactionLocks.get(transactionName));
            } else {
                return null;
            }
        } finally {
            transactionLock.readLock().unlock();
        }
    }

    public void cancelTransaction(String transactionName) {
        transactionLock.writeLock().lock();
        try {
            transactions.remove(transactionName);
            transactionLocks.remove(transactionName);
            transactionsChanges.remove(transactionName);
        } finally {
            transactionLock.writeLock().unlock();
        }
    }

    public String generateTransactionName(String tableName) {
        transactionLock.writeLock().lock();
        try {
            for (int transactionId = 0; transactionId < MAX_TRANSACTION_ID; transactionId ++) {
                String transactionName = String.format("%05d", transactionId);
                if (!transactions.containsKey(transactionName)) {
                    MyTable table = tableProvider.getTable(tableName);
                    transactions.put(transactionName, table);
                    transactionsChanges.put(transactionName, new HashMap<String, Storeable>());
                    transactionLocks.put(transactionName, new ReentrantLock());
                    return transactionName;
                }
            }
            return null;
        } finally {
            transactionLock.writeLock().unlock();
        }
    }
}
