package ru.fizteh.fivt.students.zinnatullin.storeable;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.xml.stream.XMLStreamException;

import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;
import ru.fizteh.fivt.storage.structured.TableProvider;
import ru.fizteh.fivt.students.zinnatullin.basicclasses.BasicTableProvider;

public class StoreableTableProvider extends BasicTableProvider<StoreableTable> implements TableProvider, AutoCloseable {
    private StoreableTableProviderFactory factory;
	private ReadWriteLock providerLock = new ReentrantReadWriteLock(true);
    
	public StoreableTableProvider(StoreableTableProviderFactory factory, String root) throws IOException {
	    super(root);
	    this.factory = factory;
	}
	
	public StoreableTable createTable(String name, List<Class<?>> columnTypes) throws IOException {
	    providerCloseCheck();
	    
	    if ((name == null) || (!(name.matches("\\w+")))) {
			throw new IllegalArgumentException("wrong table name " + name);
		}
		
		if ((columnTypes == null) || (columnTypes.isEmpty())) {
			throw new IllegalArgumentException("incorrect column types");
		}

		providerLock.writeLock().lock();
		try {
		    if (tables.containsKey(name)) {
		        return null;
		    } else {
		        if (!(new File(root, name)).mkdir()) {
		            throw new IllegalStateException("unable to make directory " + name);
		        }
		        
		        try (PrintStream writtenSignature = new PrintStream(new File(root + File.separator + name, "signature.tsv"))) {
		            for (int column = 0; column < columnTypes.size(); ++column) {
		                if (column != 0) {
		                    writtenSignature.print(" ");
		                }
		                Class<?> type = columnTypes.get(column);
		                if (type == null) {
		                    throw new IllegalArgumentException("wrong column type");
		                }	
		                String typeName = null;
		                typeName = TypeName.getAppropriateName(type);

		                writtenSignature.print(typeName);
		            }
			    }
			
			    try {
				    tables.put(name, new StoreableTable(root + File.separator + name, name, this, columnTypes));
			    } catch (IOException catchedException) {
			        throw new IllegalStateException(catchedException);
			    }
		    }	
			return tables.get(name);
		} finally {
			providerLock.writeLock().unlock();
		}
	}
	
	public StoreableTable getTable(String name) {    
	    providerCloseCheck();
	    
        if ((name == null) || (!(name.matches("\\w+")))) {
            throw new IllegalArgumentException("wrong table name: " + name);
        }
        
        providerLock.writeLock().lock();     
        
        try {
            if ((tables.get(name) == null) && (new File(root, name).isDirectory())) {
                try {
                    tables.put(name, new StoreableTable(root + File.separator + name, name, this));
                    tables.get(name).getFilesMap().readData();
                } catch (IOException catchedException) {
                    //do nothing
                }
            }
            return tables.get(name);
        } finally {     
            providerLock.writeLock().unlock();
        }
    }
		
	public StoreableImplementation deserialize(Table table, String value) throws ParseException {
	    providerCloseCheck();
	    
	    providerLock.readLock().lock();
		try {
			return StoreableUtils.deserialize(table, value);
		} catch (XMLStreamException catchedException) {
			throw new ParseException(catchedException.getMessage(), 0);
		} finally {
            providerLock.readLock().unlock();
        }
	}

	public String serialize(Table table, Storeable value) throws ColumnFormatException {
	    providerCloseCheck();
	    
	    providerLock.readLock().lock();
		try {
			return StoreableUtils.serialize(table, value);
		} catch (XMLStreamException catchedException) {
			throw new ColumnFormatException(catchedException);
		} finally {
		    providerLock.readLock().unlock();
		}
	}

	public StoreableImplementation createFor(Table table) {
	    providerCloseCheck();
	    
	    providerLock.readLock().lock();
		try {
		    return new StoreableImplementation(table);
		} finally {
		    providerLock.readLock().unlock();
		}
	}

	public StoreableImplementation createFor(Table table, List<?> values) throws ColumnFormatException {
	    providerCloseCheck();
	    
	    StoreableImplementation builtStoreable = null;
	    
	    providerLock.readLock().lock();	   
	    try {
	        if (table.getColumnsCount() != values.size()) {
	            throw new IndexOutOfBoundsException();
	        }
	        builtStoreable = createFor(table);
	    } finally {		
	        providerLock.readLock().unlock();
	    }
		
		for (int column = 0; column < values.size(); ++column) {
			builtStoreable.setColumnAt(column, values.get(column));
		}

		return builtStoreable;
	}
	
	public String toString() {
        return getClass().getSimpleName() + "[" + (new File(root)).getAbsolutePath() + "]";
    }
	
    public void close() {
        if (!(closeIndicator)) {
            factory.closeTableProvider(this);
            
            for (String key: tables.keySet()) {
                tables.get(key).close();
            }   
            closeIndicator = true;
        }
    }

	public void readFilesMaps() throws IOException {
		for (String subdirectory : (new File(root)).list()) {
			if (!((new File(root, subdirectory)).isDirectory())) {
				throw new IOException("wrong directory " + subdirectory);
			} else {
				tables.put(subdirectory, new StoreableTable(root + File.separator + subdirectory, subdirectory, this));
				tables.get(subdirectory).getFilesMap().readData();
			}
		}
	}

	public void writeFilesMaps() throws IOException {
		for (String key: tables.keySet()) {
			StoreableTable table = tables.get(key);
			table.autoCommit();
			table.getFilesMap().writeData();
		}
	}
	
	public StoreableTable createTable(String name) {
		return null;
	}
}
