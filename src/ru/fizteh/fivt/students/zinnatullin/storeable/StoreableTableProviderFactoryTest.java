package ru.fizteh.fivt.students.zinnatullin.storeable;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class StoreableTableProviderFactoryTest {
	@Test(expected = IllegalArgumentException.class)
	public void createTableProviderWithNullParameter() throws IOException {
		StoreableTableProviderFactory factory = new StoreableTableProviderFactory();
		factory.create(null);
		factory.close();
	}	

    @Test (expected = IllegalStateException.class)
    public void createFromClosed() throws IOException {
        StoreableTableProviderFactory factory = new StoreableTableProviderFactory();
        factory.close();
        File tempDirectory = null;
        try {
            tempDirectory = File.createTempFile("StoreableTableProviderFactoryTest", null);
        } catch (IOException catchedException) {
            return;
        }
        if (!tempDirectory.delete()) {
            return;
        }
        if (!tempDirectory.mkdir()) {
            return;
        }
        factory.create(tempDirectory.getPath());
    }
    
    @Test
    public void closeTwice() {
        StoreableTableProviderFactory factory = new StoreableTableProviderFactory();
        factory.close();
        factory.close();
    }
}
