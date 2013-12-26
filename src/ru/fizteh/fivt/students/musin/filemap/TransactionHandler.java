package ru.fizteh.fivt.students.musin.filemap;

import ru.fizteh.fivt.storage.structured.Storeable;

import java.util.HashMap;

public class TransactionHandler {
    private Transaction transaction;
    private TransactionPool transactionPool;
    private MultiFileMap table;
    private String id;

    public TransactionHandler(MultiFileMap table, TransactionPool transactionPool) {
        transaction = null;
        this.transactionPool = transactionPool;
        this.table = table;
    }

    public HashMap<String, Storeable> getDiff() {
        if (transaction == null) {
            id = transactionPool.createTransaction(table);
            transaction = transactionPool.getTransaction(id);
        }
        return transaction.getDiff();
    }

    public void clear() {
        if (transaction != null) {
            transactionPool.removeTransaction(id);
            transaction = null;
        }
    }

    public boolean isSet() {
        return transaction != null;
    }
}
