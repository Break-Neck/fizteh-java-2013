package ru.fizteh.fivt.students.kocurba.shell.command;

import java.io.File;
import java.io.IOException;

import ru.fizteh.fivt.students.kocurba.shell.StateWrap;

public class MkdirCommand implements Command<File> {

    @Override
    public int getArgCount() {
        return 1;
    }

    @Override
    public String getCommandName() {
        return "mkdir";
    }

    @Override
    public void executeCommand(StateWrap<File> state, String[] arguments)
            throws IOException {

        File dir = new File(state.getState(), arguments[1]);

        if (!dir.mkdir()) {
            throw new IOException("mkdir: mkdir error");
        }
    }
}
