package ru.fizteh.fivt.students.zhidkovanton.storable;

import ru.fizteh.fivt.storage.structured.TableProvider;
import ru.fizteh.fivt.storage.structured.TableProviderFactory;

import java.io.File;

public class DataFactoryProvider implements TableProviderFactory {

    @Override
    public TableProvider create(String dir) {
        if ((dir == null) || dir.trim().equals("")) {
            throw new IllegalArgumentException("Bad base directory");
        }
        File directoryFile = new File(dir);
        if (!directoryFile.exists()) {
            if (!directoryFile.mkdir()) {
                throw new IllegalArgumentException("Can't create" + dir);
            }
        }
        if (!directoryFile.isDirectory()) {
            throw new IllegalArgumentException("Hmm... It isn't a directory" + dir);
        }
        return new DataFactory(dir);
    }
}
