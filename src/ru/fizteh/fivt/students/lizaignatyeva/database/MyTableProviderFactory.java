package ru.fizteh.fivt.students.lizaignatyeva.database;

import ru.fizteh.fivt.storage.structured.TableProviderFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MyTableProviderFactory implements TableProviderFactory {
    private boolean isValidName(String name) {
        return name != null && !name.equals("");
    }

    @Override
    public MyTableProvider create(String dir) throws IOException {
        if (!isValidName(dir)) {
            throw new IllegalArgumentException("TableProviderFactory.create: name '" + dir + "' is invalid");
        }
        if (dir.isEmpty()) {
            throw new IOException("TableProviderFactory.create: path is empty");
        }
        Path path = Paths.get(dir);
        if (!path.toFile().exists()) {
            try {
                FileUtils.mkDir(path.toFile().getCanonicalPath());
            } catch (Exception e) {
                throw new IOException("TableProviderFactory.create: there is no directory " + dir, e);
            }
        }
        if (!path.toFile().isDirectory()) {
            throw new IllegalArgumentException("TableProviderFactory.create: " + dir + " is not a directory");
        }
        return new MyTableProvider(path);
    }
}
