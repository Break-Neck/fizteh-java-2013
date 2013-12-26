package ru.fizteh.fivt.students.dmitryKonturov.dataBase.databaseImplementation;

import ru.fizteh.fivt.storage.structured.Storeable;

import java.util.HashMap;
import java.util.Map;

public class TransactionPool {
    private Map<Integer, String> transactionTableName = new HashMap<>();
    private Map<Integer, Map<String, Storeable>> transactionDiff = new HashMap<>();
    private int lastId = 0;
    private static final int MAX_TRANSACTION_ID = 100000; // id in range [0, MaxTransaction)

    public static int getMaxTransactionId() {
        return MAX_TRANSACTION_ID;
    }

    public Map<String, Storeable> getDiffTable(int transactionId) {
        return transactionDiff.get(transactionId);
    }

    public String getTableName(int transactionId) {
        return transactionTableName.get(transactionId);
    }

    public boolean transactionExists(int transactionId) {
        return transactionDiff.containsKey(transactionId);
    }

    public int createTransaction(String tableName) {
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

    public void deleteTransaction(int transactionId) {
        transactionDiff.remove(transactionId);
        transactionTableName.remove(transactionId);
    }
}
