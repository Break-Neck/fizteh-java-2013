package ru.fizteh.fivt.students.fedoseev.filemap;

import ru.fizteh.fivt.students.fedoseev.common.AbstractCommand;

import java.io.IOException;

public class FileMapGetCommand extends AbstractCommand<FileMapState> {
    public FileMapGetCommand() {
        super("get", 1);
    }

    @Override
    public void execute(String[] input, FileMapState state) throws IOException {
        String value = AbstractFileMap.getContent().get(input[0]);

        if (value == null) {
            System.out.println("not found");
        } else {
            System.out.println("found\n" + value);
        }
    }
}
