package ru.fizteh.fivt.students.zinnatullin.junit;

import java.io.File;
import ru.fizteh.fivt.storage.strings.Table;
import ru.fizteh.fivt.storage.strings.TableProvider;

public class DBTableProvider implements TableProvider{

	private File path;
	
	public DBTableProvider(File path) {
		this.path = path;
	}

	@Override
	public Table getTable(String name) {
		File tablePath = new File(path, name);
		if(tablePath.exists() && tablePath.isDirectory()){
			return new DBTable(name, tablePath);
		}
		return null;
	}

	@Override
	public Table createTable(String name) {
		File tablePath = new File(path, name);
		if(tablePath.exists() && tablePath.isDirectory()){
			return null;
		}
		if(!tablePath.isDirectory()){
			tablePath.delete();
		}
		tablePath.mkdir();
		return new DBTable(name, tablePath);
	}

	@Override
	public void removeTable(String name) {
		File tablePath = new File(path, name);
		if(tablePath.exists() && tablePath.isDirectory()){
			tablePath.delete();
		}
	}
}