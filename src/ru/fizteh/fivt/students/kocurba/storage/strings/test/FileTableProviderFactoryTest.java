package ru.fizteh.fivt.students.kocurba.storage.strings.test;

import org.junit.*;
import org.junit.rules.TemporaryFolder;
import ru.fizteh.fivt.storage.strings.TableProviderFactory;
import ru.fizteh.fivt.students.kocurba.storage.strings.FileTableProviderFactory;

/**
 * Task 05 - JUnit
 * 
 * JUnit tests for {@link FileTableProviderFactory} class
 * 
 * @author Alina Kocurba
 * 
 */
public class FileTableProviderFactoryTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test(expected = IllegalArgumentException.class)
    public void testCreateNull() {
        TableProviderFactory factory = new FileTableProviderFactory();
        factory.create(null);
    }

}
