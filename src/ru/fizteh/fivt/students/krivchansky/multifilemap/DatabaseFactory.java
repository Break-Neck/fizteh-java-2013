package src.ru.fizteh.fivt.students.krivchansky.multifilemap;


import java.io.File;

public class DatabaseFactory implements TableProviderFactory {
    public TableProvider create(String directory) {
    	if (directory.isEmpty() || directory == null) {
    		throw new IllegalArgumentException ("directory name cannot be null");
    	}
    	File databaseDirectory = new File(directory);
    	if (databaseDirectory.isFile()) {
    		throw new IllegalArgumentException ("it must be directory, not file");
    	}
    	if (!databaseDirectory.exists()) {
    		databaseDirectory.mkdir();
    	}
        return new Database(databaseDirectory.getAbsolutePath());
    }
}
