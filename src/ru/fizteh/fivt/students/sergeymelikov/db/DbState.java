package ru.fizteh.fivt.students.sergeymelikov.db;

import java.io.IOException;
import ru.fizteh.fivt.students.sergeymelikov.shell.StateInterface;

public interface DbState extends StateInterface {

    String getValue(String key) throws IOException;

    String removeValue(String key) throws IOException;

    String put(String key, String value) throws IOException;

    int commitDif() throws IOException;

}
