package ru.fizteh.fivt.students.zhidkovanton.JUnit;

import ru.fizteh.fivt.storage.strings.TableProviderFactory;
import ru.fizteh.fivt.storage.strings.TableProvider;
import java.io.File;

public class DataFactoryProvider implements TableProviderFactory {

    @Override
    public TableProvider create(String dir) {
        if ((dir == null) || dir.trim().equals("")) {
            throw new IllegalArgumentException();
        }
        File directoryFile = new File(dir);
        if (!directoryFile.exists()) {
            if (!directoryFile.mkdir()) {
                throw new IllegalArgumentException();
            }
        }
        if (!directoryFile.isDirectory()) {
            throw new IllegalArgumentException();
        }
        return new DataFactory(dir);
    }
}
