package src.ru.fizteh.fivt.students.krivchansky.filemap;

import java.io.File;
import java.io.IOException;


public class SingleFileTable extends TableUsingStrings{
    
    public static final String DATABASENAME = "db.dat";

    public SingleFileTable(String directory, String tableName) {
    	super(directory, tableName);
    }
    
    protected void load() throws IOException {
        FileMapReadingUtils.scanFromDisk(getPathToDatabase(), new SimpleTableBuilder(this));
    }
    
    protected void save() throws IOException {
        FileMapWritingUtils.writeOnDisk(unchangedOldData.keySet(), getPathToDatabase(), new SimpleTableBuilder(this));
    }
    
    private String getPathToDatabase() {
    	File file = new File(getParentDirectory(), DATABASENAME);
    	return file.getAbsolutePath();
    }
    

}
