import ru.fizteh.fivt.students.krivchansky.shell;
package ru.fizteh.fivt.students.krivchansky.filemap;

import java.io.File;


public class SingleFileTable extends SomeTable {
    
    protected static final String DATABASENAME = "db.dat";

    public SingleFileTable(String dir, String name) {
        super(dir, name);
    }
    
    protected void load() throws SomethingIsWrongException {
        scanFromDisk(getPathToDatabase());
    }
    
    protected void save() throws SomethingIsWrongException {
        writeOnDisk(unchangedOldData.keySet(), getPathToDatabase());
    }
    
    private String getPathToDatabase() {
        File databaseFile = new File (getParentDirectory(), DATABASENAME);
        return databaseFile.getAbsolutePath();
    }
    

}
