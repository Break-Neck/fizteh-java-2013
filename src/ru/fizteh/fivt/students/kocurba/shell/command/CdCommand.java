package ru.fizteh.fivt.students.kocurba.shell.command;

import ru.fizteh.fivt.students.kocurba.shell.StateWrap;
import java.io.File;
import java.io.IOException;

public class CdCommand implements Command<File> {
    @Override
    public int getArgCount() {
        return 1;
    }

    @Override
    public String getCommandName() {
        return "cd";
    }

    @Override
    public void executeCommand(StateWrap<File> state, String[] arguments) throws IOException {
        File newDir = new File(arguments[1]);
        if (!newDir.isAbsolute()) {
            newDir = new File(state.getState(), arguments[1]);
        }
        if (!newDir.exists() | !newDir.isDirectory()) {
            throw new IOException("cd: directory does not exist");
        } else {
            state.setState(newDir.getCanonicalFile());
        }
    }
}
