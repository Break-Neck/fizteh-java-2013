package ru.fizteh.fivt.students.kamilTalipov.database.core;

import ru.fizteh.fivt.storage.structured.TableProviderFactory;

import java.io.FileNotFoundException;
import java.io.IOException;

public class MultiFileHashTableFactory implements TableProviderFactory {
    @Override
    public MultiFileHashTableProvider create(String dir) throws IllegalArgumentException, IOException {
        if (dir == null) {
            throw new IllegalArgumentException("Directory path must be not null");
        }

        try {
            return new MultiFileHashTableProvider(dir);
        } catch (DatabaseException e) {
            throw new IllegalArgumentException("Database error", e);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("File not found", e);
        }
    }
}
