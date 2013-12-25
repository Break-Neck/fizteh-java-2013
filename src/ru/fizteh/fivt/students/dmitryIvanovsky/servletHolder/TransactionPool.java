package ru.fizteh.fivt.students.dmitryIvanovsky.servletHolder;

import ru.fizteh.fivt.students.dmitryIvanovsky.fileMap.MyHashMap;

import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TransactionPool {
    HashMap<Integer, MyHashMap> allMap = new HashMap<>();
    int randomNumber = 0;
    private static final int MAX = 100000;
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock write = readWriteLock.writeLock();
    HashMap<Integer, String> transactionTable = new HashMap<>();

    public MyHashMap getMap(int numberTransaction) {
        write.lock();
        try {
            return allMap.get(numberTransaction);
        } finally {
            write.unlock();
        }
    }

    public void deleteTransaction(int numberTransaction) {
        write.lock();
        try {
            transactionTable.remove(numberTransaction);
            allMap.remove(numberTransaction);
        } finally {
            write.unlock();
        }

    }

    public int createNewTransaction(String nameTable) {
        write.lock();
        try {
            while (allMap.containsKey(randomNumber)) {
                randomNumber = (randomNumber + 1) % MAX;
            }
            allMap.put(randomNumber, new MyHashMap());
            transactionTable.put(randomNumber, nameTable);
            return randomNumber;
        } finally {
            write.unlock();
        }
    }

    public String getNameTable(int numberTransaction) {
        write.lock();
        try {
            return transactionTable.get(numberTransaction);
        } finally {
            write.unlock();
        }
    }

    public boolean isExistTransaction(int numberTransaction) {
        write.lock();
        try {
            return allMap.containsKey(numberTransaction);
        } finally {
            write.unlock();
        }
    }
}
