package ru.fizteh.fivt.students.vlmazlov.filemap;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import ru.fizteh.fivt.students.vlmazlov.multifilemap.GenericTableProvider;
import ru.fizteh.fivt.students.vlmazlov.multifilemap.ProviderWriter;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReadWriteLock;
import ru.fizteh.fivt.students.vlmazlov.multifilemap.ValidityCheckFailedException;
import ru.fizteh.fivt.students.vlmazlov.multifilemap.ValidityChecker;

public abstract class GenericTable<V> implements Iterable<Map.Entry<String, V>>, Cloneable {
    private Map<String, V> commited;
    protected GenericTableProvider<V, ? extends GenericTable<V>> provider;

    private final ThreadLocal<HashMap<String, V>> changed = new ThreadLocal<HashMap<String, V>>() {
        protected HashMap<String, V> initialValue() {
            return new HashMap<String, V>();
        }
    };

    private final ThreadLocal<HashSet<String>> deleted = new ThreadLocal<HashSet<String>>() {
        protected HashSet<String> initialValue() {
            return new HashSet<String>();
        }
    };

    private final String name;
    protected final boolean autoCommit;

    private ReadWriteLock getCommitLock;

    public GenericTable(GenericTableProvider<V, ? extends GenericTable<V>> provider, String name) {
        this(provider, name, true);
    }

    public GenericTable(GenericTableProvider<V, ? extends GenericTable<V>> provider, String name, boolean autoCommit) {
        this.name = name;
        this.provider = provider;	
        commited = new HashMap<String, V>();
        
        this.autoCommit = autoCommit;
        //fair queue
        getCommitLock = new ReentrantReadWriteLock(true);
    }

    public Iterator iterator() {
        return commited.entrySet().iterator();
    }

    public V put(String key, V value) {
        try {
            ValidityChecker.checkTableKey(key);
            ValidityChecker.checkTableValue(value);
        } catch (ValidityCheckFailedException ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }

        V returnValue = get(key); 

        //putting the same value as in the last commited version
        //effectively discards any changes made to it
        //anyway, local changes should be applied no matter what
        changed.get().put(key, value);

        //the value put back is no longer deleted
        deleted.get().remove(key);

        if (autoCommit) {
            commit();
        }
        return returnValue;
    }

    public V get(String key) {
        try {
            ValidityChecker.checkTableKey(key);
        } catch (ValidityCheckFailedException ex) {
            throw new IllegalArgumentException(ex.getMessage());
       	}

        if (deleted.get().contains(key)) {
            return null;
        }

        if (changed.get().get(key) != null) {
            return changed.get().get(key);
        }

        getCommitLock.readLock().lock();

        try {
        	if (commited.get(key) != null) { 
            	return commited.get(key);      
        	}
        } finally {
            getCommitLock.readLock().unlock();
       	}

        //redundant but still
        return null;
    }

    public V remove(String key) {
        try {
            ValidityChecker.checkTableKey(key);
        } catch (ValidityCheckFailedException ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }

        V returnValue = get(key);
        V commitedValue = null;

        getCommitLock.readLock().lock();

        try {
        	commitedValue = commited.get(key);
        } finally {
            getCommitLock.readLock().unlock();
        } 

        //if present, the key should be deleted from a commited version of a table
        if (commitedValue != null) {
        	deleted.get().add(key);
        }
        //it is deleted from local changes regardless
       	changed.get().remove(key);

        if (autoCommit) {
            commit();
        }
        return returnValue;
    }

    public int size() {
    	int size = 0;
    	getCommitLock.readLock().lock();

    	try {
	        size = commited.size();

	    	for (Map.Entry<String, V> entry: changed.get().entrySet()) {
			    if (commited.get(entry.getKey()) == null) {
			    	++size;
			    }
		    }

		    for (String entry : deleted.get()) {
		    	if (commited.get(entry) != null) {
			        --size;
		   		}
		    }
		} finally {
   			getCommitLock.readLock().unlock();
   		}

        return size;
   	}

    public String getName() {
        return name;
    }

    public int commit() {
    	int diffNum;

        getCommitLock.writeLock().lock();

        try {
            diffNum = getDiffCount();

	        pushChanges();

            ProviderWriter.writeMultiTable(this, new File(provider.getRoot(), getName()), provider);    

	    } catch (IOException ex) {
            throw new RuntimeException("Unable to write table to the disc: " + ex.getMessage());
        } catch (ValidityCheckFailedException ex) {
            throw new RuntimeException("Validity check failed: " + ex.getMessage());
        } finally {

	        getCommitLock.writeLock().unlock();
    	}

        changed.get().clear();
        deleted.get().clear();

        return diffNum;
    }

    public int rollback() {
        int diffNum;
        
        getCommitLock.readLock().lock();
        try {
    	   diffNum = getDiffCount();
        } finally {
            getCommitLock.readLock().unlock();
        }

        changed.get().clear();
        deleted.get().clear();

        return diffNum;
    }

    //should be locked from the outside, unless made sure that the object is thread-unique
    //synchronized, just to be safe

    public synchronized void pushChanges() {
        for (Map.Entry<String, V> entry: changed.get().entrySet()) {
            commited.put(entry.getKey(), entry.getValue());
        }

        for (String entry : deleted.get()) {
            commited.remove(entry);
        }
    }

    //should be locked from the outside, unless made sure that the object is thread-unique 
    public int getDiffCount() {

    	int diffCount = 0;
    	
        for (Map.Entry<String, V> entry: changed.get().entrySet()) {    

		    if ((commited.get(entry.getKey()) == null) ||
                (!isValueEqual(entry.getValue(), commited.get(entry.getKey())))) {
                
                ++diffCount;
			}
		}

		for (String entry : deleted.get()) {
			if (commited.get(entry) != null) {
                ++diffCount; 
			}
		}
        return diffCount;
   	}

    public abstract GenericTable<V> clone();

    //both != null
    protected boolean isValueEqual(V first, V second) {
    	return first.equals(second);
    }

    public abstract void checkRoot(File root) throws ValidityCheckFailedException;
}