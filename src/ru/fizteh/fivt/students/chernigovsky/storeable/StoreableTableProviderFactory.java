package ru.fizteh.fivt.students.chernigovsky.storeable;

import ru.fizteh.fivt.storage.structured.TableProviderFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class StoreableTableProviderFactory implements TableProviderFactory, AutoCloseable {
    private final ArrayList<StoreableTableProvider> tableProviderList;
    private boolean isClosed;

    public StoreableTableProviderFactory() {
        tableProviderList = new ArrayList<StoreableTableProvider>();
        isClosed = false;
    }

    @Override
    public StoreableTableProvider create(String dir) throws IOException {
        if (dir == null || dir.isEmpty()) {
            throw new IllegalArgumentException("dir is null");
        }

        File dbDirectory = new File(dir);
        if (dbDirectory.exists() && !dbDirectory.isDirectory()) {
            throw new IllegalArgumentException("not a dir");
        }
        if (!dbDirectory.exists()) {
            throw new IOException("no such directory");
        }

        StoreableTableProvider tableProvider = new StoreableTableProvider(dbDirectory, false);
        tableProviderList.add(tableProvider);
        return tableProvider;
    }

    public synchronized void close() {
        if (isClosed) {
            return;
        }
        for (StoreableTableProvider tableProvider : tableProviderList) {
            tableProvider.close();
        }
        isClosed = true;
    }

}