package ru.fizteh.fivt.students.dzvonarev.filemap;

import java.util.HashMap;
import java.util.Map;

public class TransactionManager {

    private Map<String, Transaction> changesMap;
    private MyTableProvider currentProvider;  // for starting transaction
    private int numberOfTransactions;

    public TransactionManager(MyTableProvider provider) {
        currentProvider = provider;
        changesMap = new HashMap<>();
        numberOfTransactions = 0;
    }

    public String getTransactionId() {
        String numTrans = (new Integer(numberOfTransactions)).toString();
        int transLen = numTrans.length();
        StringBuilder builder = new StringBuilder();
        while (transLen < 5) {
            builder.append(0);
            ++transLen;
        }
        builder.append(numTrans);
        ++numberOfTransactions;
        return builder.toString();
    }

    public String startTransaction(String name) {
        Transaction transaction = new Transaction(currentProvider, name, this);
        changesMap.put(transaction.getId(), transaction);
        return transaction.getId();
    }

    void stopTransaction(String id) {
        changesMap.remove(id);
    }

    public Transaction getTransaction(String id) {
        return changesMap.get(id);
    }

}
