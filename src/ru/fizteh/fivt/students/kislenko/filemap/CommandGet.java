package ru.fizteh.fivt.students.kislenko.filemap;

import ru.fizteh.fivt.students.kislenko.shell.Command;

import java.io.FileNotFoundException;

public class CommandGet implements Command<FilemapState> {
    public String getName() {
        return "get";
    }

    public int getArgCount() {
        return 1;
    }

    public void run(FilemapState state, String[] args) throws FileNotFoundException {
        if (state.hasKey(args[0])) {
            System.out.println("found\n" + state.getValue(args[0]));
        } else {
            System.out.println("not found");
        }
    }
}