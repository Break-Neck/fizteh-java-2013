package src.ru.fizteh.fivt.students.krivchansky.multifilemap;

import java.io.File;

import src.ru.fizteh.fivt.students.krivchansky.filemap.GlobalUtils;
import src.ru.fizteh.fivt.students.krivchansky.filemap.SimpleTableBuilder;
import src.ru.fizteh.fivt.students.krivchansky.filemap.TableUsingStrings;

public class MultiFileMapTableBuilder extends SimpleTableBuilder {
	private int currentDir;
	private int currentFile;
	
	
	public MultiFileMapTableBuilder(TableUsingStrings table) {
		super(table);
	}
	
	public void setCurrentFile(File file) {
		currentDir = GlobalUtils.parseDirNumber(file.getParentFile());
		currentFile = GlobalUtils.parseFileNumber(file);
	}
	
	public void put(String key, String value) {
		GlobalUtils.checkKeyPlacement(key, currentDir, currentFile);
		super.put(key, value);
	}
}
