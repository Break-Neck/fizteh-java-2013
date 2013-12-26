package ru.fizteh.fivt.students.zhidkovanton.storable;

import org.junit.*;
import org.junit.rules.TemporaryFolder;
import ru.fizteh.fivt.storage.structured.TableProviderFactory;

import java.io.IOException;

public class MyTableProviderFactoryTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test(expected = IllegalArgumentException.class)
    public void testCreateNull() throws IOException {
        TableProviderFactory factory = new DataFactoryProvider();
        factory.create(null);
    }

    @Test
    public void testCreateNotNull() throws IOException {
        TableProviderFactory factory = new DataFactoryProvider();
        Assert.assertNotNull(factory.create(folder.newFolder("folder").getCanonicalPath()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEmpty() throws IOException {
        TableProviderFactory factory = new DataFactoryProvider();
        Assert.assertNotNull(factory.create(""));
    }

    @Test (expected = IllegalStateException.class)
    public void testClose() throws Exception {
        TableProviderFactory factory = new DataFactoryProvider();
        ((AutoCloseable) factory).close();
        factory.create("asdasd");
    }
}
