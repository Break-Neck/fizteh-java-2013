package ru.fizteh.fivt.students.kocurba.storeable;

import ru.fizteh.fivt.storage.structured.TableProviderFactory;

import java.io.IOException;
import java.io.File;

public class StoreableTableProviderFactory implements TableProviderFactory {

    @Override
    public StoreableTableProvider create(String path) throws IOException {
        if ((path == null) || path.trim().equals("")) {
            throw new IllegalArgumentException("Bad base directory");
        }
        File directoryFile = new File(path);
        if (!directoryFile.exists()) {
            if (!directoryFile.mkdir()) {
                throw new IOException("Can't create" + path);
            }
        }
        if (!directoryFile.isDirectory()) {
            throw new IllegalArgumentException("Hmm... It isn't a directory" + path);
        }
        return  new StoreableTableProvider(path);

    }
}
