package ru.fizteh.fivt.students.krivchansky.multifilemap;

import ru.fizteh.fivt.storage.strings.TableProviderFactory;

public interface MyTableProviderFactory extends TableProviderFactory {
	MyTableProvider create(String dir);
}
