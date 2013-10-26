package ru.fizteh.fivt.students.krivchansky.multifilemap;

import ru.fizteh.fivt.students.krivchansky.filemap.*;

import java.io.File;

public class DatabaseFactory {
    public Database create(String directory) {
    	File databaseDirectory = new File(directory);
    	if (!databaseDirectory.exists()) {
    		databaseDirectory.mkdir();
    	}
        return new Database(databaseDirectory.getAbsolutePath());
    }
}
