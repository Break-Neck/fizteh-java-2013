package ru.fizteh.fivt.students.zinnatullin.storable;

import java.io.File;
import ru.fizteh.fivt.storage.strings.TableProvider;
import ru.fizteh.fivt.storage.strings.TableProviderFactory;

public class DBTableProviderFactory implements TableProviderFactory{

	@Override
	public TableProvider create(String dir) {
		
		File path = new File(dir);
		if(!path.exists() || !path.isDirectory()){
			return null;
		}
		
		return new DBTableProvider(path);
	}
}
