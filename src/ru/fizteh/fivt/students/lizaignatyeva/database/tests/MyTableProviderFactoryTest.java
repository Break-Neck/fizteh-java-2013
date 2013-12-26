package ru.fizteh.fivt.students.lizaignatyeva.database.tests;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.fizteh.fivt.students.lizaignatyeva.database.MyTableProviderFactory;

import java.io.IOException;

import static junit.framework.Assert.assertNotNull;

public class MyTableProviderFactoryTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private MyTableProviderFactory factory;

    @Before
    public void init() throws IOException {
        factory = new MyTableProviderFactory();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullPath() throws IOException {
        factory.create(null);
    }

    @Test(expected = IOException.class)
    public void testCreateBadPath() throws IOException {
        factory.create("/there/are/no/such/dir");
    }

    @Test
    public void testBasic() throws IOException {
        assertNotNull(factory.create(temporaryFolder.getRoot().getCanonicalPath()));
    }
}
