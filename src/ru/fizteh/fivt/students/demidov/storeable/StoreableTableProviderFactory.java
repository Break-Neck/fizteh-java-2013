package ru.fizteh.fivt.students.demidov.storeable;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import ru.fizteh.fivt.storage.structured.TableProviderFactory;

public class StoreableTableProviderFactory implements TableProviderFactory, AutoCloseable {
    private boolean closeIndicator = false;
    private Set<StoreableTableProvider> createdTableProviders;
    
    public StoreableTableProviderFactory() {
        closeIndicator = false;
        createdTableProviders = new HashSet<>();
    }
	
	public StoreableTableProvider create(String dir) throws IOException {	
	    providerFactoryCloseCheck();
	    
		if ((dir == null) || (dir.trim().isEmpty())) {
			throw new IllegalArgumentException("wrong dir");
		}
		if ((!(new File(dir)).exists()) && (!(new File(dir)).mkdir())) {
			throw new IOException("non-existing dir");
		}
		
		StoreableTableProvider createdProvider = new StoreableTableProvider(this, dir);
		
		createdTableProviders.add(createdProvider);
		
		return createdProvider;			
	}
	
    public void closeTableProvider(StoreableTableProvider tableProvider) {
        providerFactoryCloseCheck();
        createdTableProviders.remove(tableProvider);
    }  
	
    private void providerFactoryCloseCheck() {
        if (closeIndicator) {
            throw new IllegalStateException("table provider factory is closed");
        }
    }
	
	public void close() {
	    if (!(closeIndicator)) {	        
	        for (StoreableTableProvider currentProvider: createdTableProviders) {
	            currentProvider.close();
	        }
	        closeIndicator = true;
	    }
	}
}
