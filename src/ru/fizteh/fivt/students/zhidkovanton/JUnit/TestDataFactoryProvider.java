package ru.fizteh.fivt.students.zhidkovanton.JUnit;

import org.junit.*;
import org.junit.rules.TemporaryFolder;
import ru.fizteh.fivt.storage.strings.TableProviderFactory;
import java.io.IOException;


public class TestDataFactoryProvider {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testCreateNotNull() throws IOException {
        TableProviderFactory factory = new DataFactoryProvider();
        Assert.assertNotNull(factory.create(folder.newFolder("folder").getCanonicalPath()));
    }
    @Test(expected = IllegalArgumentException.class)
    public void testCreateNull() {
        TableProviderFactory factory = new DataFactoryProvider();
        factory.create(null);
    }


}
