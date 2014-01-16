package ru.fizteh.fivt.students.zinnatullin.filemap;

import java.io.IOException;

import ru.fizteh.fivt.students.zinnatullin.basicclasses.BasicState;

public class FileMapState implements BasicState {
	private FileMap<String> filemap;
	
	public FileMapState(FileMap<String> filemap) {
		this.filemap = filemap;
	}

	public String get(String key) throws IOException {
		return filemap.getCurrentTable().get(key);
	}

	public String put(String key, String value) throws IOException {
		return filemap.getCurrentTable().put(key, value);
	}

	public String remove(String key) throws IOException {
		return filemap.getCurrentTable().remove(key);
	}
}
