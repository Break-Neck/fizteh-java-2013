package ru.fizteh.fivt.students.belousova.storable;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class StorableTableProviderFactory implements ExtendedTableProviderFactory, AutoCloseable {
    private Set<StorableTableProvider> tableProviderSet = new HashSet<>();
    private boolean isClosed = false;

    @Override
    public StorableTableProvider create(String path) throws IOException {
        if (isClosed) {
            throw new IllegalStateException("TableProviderFactory is closed");
        }
        if (path == null) {
            throw new IllegalArgumentException("Path to storage isn't set");
        }
        if (path.trim().isEmpty()) {
            throw new IllegalArgumentException("empty directory");
        }
        StorableTableProvider tableProvider = new StorableTableProvider(new File(path));
        tableProviderSet.add(tableProvider);
        return tableProvider;
    }

    @Override
    public void close() throws Exception {
        if (!isClosed) {
            for (StorableTableProvider tableProvider : tableProviderSet) {
                tableProvider.close();
            }
            isClosed = true;
        }
    }
}
