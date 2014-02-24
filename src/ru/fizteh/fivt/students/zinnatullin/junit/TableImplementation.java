package ru.fizteh.fivt.students.zinnatullin.junit;

import java.io.IOException;

import ru.fizteh.fivt.storage.strings.Table;
import ru.fizteh.fivt.students.zinnatullin.basicclasses.BasicTable;

public class TableImplementation extends BasicTable<String> implements Table {
	public TableImplementation(String path, String tableName) throws IOException {
		super(path, tableName);
	}

	public int commit() {
		try {
			return super.commit();
		} catch(IOException catchedException) {
			throw new RuntimeException(catchedException);
		}
	}
	
	public String put(String key, String value) {
		if ((value == null) || (value.trim().isEmpty())) {
			throw new IllegalArgumentException("empty value");
		}
		return super.put(key, value);
	}
	
	public String serialize(String value) {
		return value;
	}
	
	public String deserialize(String value) {
		return value;
	}
}
