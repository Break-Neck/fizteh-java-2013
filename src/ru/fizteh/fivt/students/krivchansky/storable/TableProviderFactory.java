package ru.fizteh.fivt.students.krivchansky.storable;

import java.io.IOException;

public interface TableProviderFactory {
    TableProvider create(String path) throws IOException;
}