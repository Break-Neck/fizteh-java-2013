package src.ru.fizteh.fivt.students.krivchansky.multifilemap;

import java.io.IOException;

import src.ru.fizteh.fivt.students.krivchansky.filemap.SimpleTableBuilder;
import src.ru.fizteh.fivt.students.krivchansky.filemap.TableUsingStrings;


public class MultifileTable extends TableUsingStrings {
	


    public MultifileTable(String directory, String tableName) {
        super(directory, tableName);
    }  

    /*private File getTableDirectory() {
		File tableDirectory = new File(getParentDirectory(), getName());
		if (!tableDirectory.exists()) {
	        tableDirectory.mkdir();
	    }
		return tableDirectory;
    }*/

	protected void load() throws IOException {
		MultiFileMapReadingUtils.load(new SimpleTableBuilder(this));
		
	}

	protected void save() throws IOException{
		MultiFileMapWritingUtils.save(new SimpleTableBuilder(this));
		
	}

}