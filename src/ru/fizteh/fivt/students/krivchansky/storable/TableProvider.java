package ru.fizteh.fivt.students.krivchansky.storable;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public interface TableProvider {

    Table getTable(String name);

    Table createTable(String name, List<Class<?>> columnTypes) throws IOException;
    
    void removeTable(String name) throws IOException;

    Storeable deserialize(Table table, String value) throws ParseException;

    String serialize(Table table, Storeable value) throws ColumnFormatException;

    Storeable createFor(Table table);

    Storeable createFor(Table table, List<?> values) throws ColumnFormatException, IndexOutOfBoundsException;
}
