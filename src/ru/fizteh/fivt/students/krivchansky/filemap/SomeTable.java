import ru.fizteh.fivt.students.krivchansky.shell;
package ru.fizteh.fivt.students.krivchansky.filemap;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public abstract class SomeTable {
    protected static HashMap<String, String> unchangedOldData;
    protected static HashMap<String, String> currentData;
    protected static HashSet<String> deletedKeys;
    private String name;
    private String parentDirectory;
    private static int size;
    protected static int unsavedChangesCounter;
    protected abstract void load() throws SomethingIsWrongException;
    protected abstract void save() throws SomethingIsWrongException;
    
    public String getParentDirectory() {
        return parentDirectory;
    }
    
    public String getName() {
        return name;
    }
    public int getSize() {
        return size;
    }
    
    public int getChangesCount() {
        return unsavedChangesCounter;
    }
    
    public SomeTable(String dir, String name) {
        this.parentDirectory = dir;
        this.name = name;
        unchangedOldData = new HashMap<String, String>();
        currentData = new HashMap<String, String>();
        deletedKeys = new HashSet<String>();
        unsavedChangesCounter = 0;
        File temp = new File(dir, name);
        if (!temp.exists()) {
            try {
                temp.createNewFile();
            } catch (IOException e) {
                System.err.println ("Unable to create a file. Message: " + e.getMessage());
            }
            
        }
        try {
            load();
        } catch (SomethingIsWrongException e) {
            System.err.println("Error aqcuired while opening a table. Message: " + e.getMessage());
        }
        
    }
    
    public String get(String key) {
        if (currentData.containsKey(key)) {
            return currentData.get(key);
        } else if (deletedKeys.contains(key)) {
            return null;
        }
        return unchangedOldData.get(key);
    }
    
    public static String put(String key, String Value) {
        String value = oldValue(key);
        currentData.put(key, Value);
        if (value == null) {
            ++size;
        }
        ++unsavedChangesCounter;
        return value;
    }
    
    public static String remove(String key) {
        String value = oldValue(key);
        if (currentData.containsKey(key)) {
            currentData.remove(key);
            if (unchangedOldData.containsKey(key)) {
                deletedKeys.add(key);
            }
        } else {
            deletedKeys.add(key);
        }
        if (value != null) {
            --size;
        }
        ++unsavedChangesCounter;
        return value;
    }
    
    private static String oldValue(String key) {
            String oldValue = currentData.get(key);
            if (oldValue == null && !deletedKeys.contains(key)) {
                oldValue = unchangedOldData.get(key);
            }
            return oldValue;
    }
    
    private int keySize(Set<String> keys) {
        int keysSize = 0;
        for (String key : keys) {
            keysSize += UtilMethods.countBytes(key, UtilMethods.ENCODING) + 5;
        }
        return keysSize;
    }
    
    protected void writeOnDisk(Set<String> keys, String file) throws SomethingIsWrongException {
        WritingUtils write = new WritingUtils(file);
        int temp = keySize(keys);
        for(String key : keys) {
            write.writeKey(key);
            write.writeOffset(temp);
            temp += UtilMethods.countBytes(unchangedOldData.get(key), UtilMethods.ENCODING);
        }
        for(String key : keys) {
            String tempCheck = unchangedOldData.get(key);
            if (tempCheck != null) {
                write.writeValue(tempCheck);   
            }
        }
        UtilMethods.closeCalm(write.dataFile);
    }
    
    protected void scanFromDisk(String file) throws SomethingIsWrongException {
        if (!UtilMethods.doesExist(file)) {
            throw new SomethingIsWrongException("Unable to scan from disc.");
        }
        ReadingUtils read = new ReadingUtils(file);
        while(!read.endOfFile()) {
            String key = read.readKey();
            String value = read.readValue();
            unchangedOldData.put(key, value);
        }
        UtilMethods.closeCalm(read.tempFile);
    }
    
    public int rollback() {
        int deletedOrAdded = Math.abs(unchangedOldData.size() - size);
        deletedKeys.clear();
        currentData.clear();
        size = unchangedOldData.size();
        unsavedChangesCounter = 0;
        return deletedOrAdded;
    }
    
    public int commit() throws SomethingIsWrongException {
        int commitCount = Math.abs(unchangedOldData.size() - size);
        for (String toDelete : deletedKeys) {
            unchangedOldData.remove(toDelete);
        }
        for (String toAdd : currentData.keySet()) {
            String tempCheck = currentData.get(toAdd);
            if (tempCheck != null) {
                unchangedOldData.put(toAdd, tempCheck);        
            }
        }
        size = unchangedOldData.size();
        save();
        unsavedChangesCounter = 0;
        deletedKeys.clear();
        currentData.clear();
        return commitCount;
    }
    
    

}
