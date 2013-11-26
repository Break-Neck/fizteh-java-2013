package ru.fizteh.fivt.students.kislenko.filemap;

import ru.fizteh.fivt.students.kislenko.shell.Command;

import java.io.FileNotFoundException;

public class CommandRemove implements Command<FilemapState> {
    public String getName() {
        return "remove";
    }

    public int getArgCount() {
        return 1;
    }

    public void run(FilemapState state, String[] args) throws FileNotFoundException {
        if (state.hasKey(args[0])) {
            state.delValue(args[0]);
            System.out.println("removed");
        } else {
            System.out.println("not found");
        }
    }
}
