package ru.fizteh.fivt.students.sergeymelikov.multifilemap;

import java.io.File;
import ru.fizteh.fivt.storage.strings.TableProviderFactory;
import ru.fizteh.fivt.students.sergeymelikov.multifilemap.extend.ExtendProvider;

public class MyTableProviderFactory implements  TableProviderFactory {
    
    @Override
    public ExtendProvider create(String dataBaseDir) {
        if (dataBaseDir == null) {
           //throw new IllegalArgumentException("dir not defined");
           System.err.println("dir not defined");
           System.exit(1);
        }
        File directory = new File(dataBaseDir);
        if (!directory.isDirectory()) {
            //throw new IllegalArgumentException(dataBaseDir + " is not a directory name");
            System.err.println(dataBaseDir + " is not a directory name");
            System.exit(1);
        }
        return new MyTableProvider(directory);
    }
}
