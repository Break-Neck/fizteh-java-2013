package ru.fizteh.fivt.students.krivchansky.storable;

import org.junit.Test;
import ru.fizteh.fivt.storage.structured.TableProviderFactory;
import ru.fizteh.fivt.students.krivchansky.storable.DatabaseTableProviderFactory;

import java.io.IOException;

public class DatabaseTableProviderFactoryTest {
    @Test(expected = IllegalArgumentException.class)
    public void createProviderNullDirectoryTest() {
        TableProviderFactory factory = new DatabaseTableProviderFactory();
        try {
            factory.create(null);
        } catch (IOException e) {
            //
        }
    }
}