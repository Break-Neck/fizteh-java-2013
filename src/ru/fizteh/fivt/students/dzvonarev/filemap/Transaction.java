package ru.fizteh.fivt.students.dzvonarev.filemap;

import ru.fizteh.fivt.storage.structured.Storeable;

import java.io.IOException;
import java.text.ParseException;

public class Transaction {

    private MyTableProvider provider;
    private MyTable table;
    private TransactionManager manager;
    private String transactionId;
    private long tableTransactionId;

    public Transaction(MyTableProvider tableProvider, String name, TransactionManager transactionManager) {
        provider = tableProvider;
        manager = transactionManager;
        table = (MyTable) provider.getTable(name);
        tableTransactionId = TransactionChanges.getInstance().createTransaction();
        transactionId = manager.getTransactionId();
    }

    /* trans operation: */

    public String getId() {
        return transactionId;
    }

    public int commit() throws IOException {
        int cntOfChanges = table.commit(tableTransactionId);
        manager.stopTransaction(transactionId);
        return cntOfChanges;
    }

    public int rollback() throws IOException {
        int cntOfChanges = table.rollback(tableTransactionId);
        manager.stopTransaction(transactionId);
        return cntOfChanges;
    }

    public String get(String key) {
        Storeable value = table.get(key, tableTransactionId);
        if (value == null) {
            throw new IllegalArgumentException("key not found");
        }
        return provider.serialize(table, value);
    }

    public String put(String key, String value) throws IOException {
        try {
            Storeable oldValue = table.put(key, provider.deserialize(table, value), tableTransactionId);
            if (oldValue == null) {
                throw new IllegalArgumentException("key not found");
            }
            return provider.serialize(table, oldValue);
        } catch (ParseException e) {
            throw new IOException("error in serialization");
        }
    }

    public int size() {
        return table.size(tableTransactionId);
    }
}
