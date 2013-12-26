package ru.fizteh.fivt.students.vishnevskiy.grep;

import ru.fizteh.fivt.file.Grep;
import ru.fizteh.fivt.file.GrepFactory;

public class MyGrepFactory implements GrepFactory {

    public MyGrepFactory() {

    }

    public Grep create(String pattern) {
        return new MyGrep(pattern);
    }
}
