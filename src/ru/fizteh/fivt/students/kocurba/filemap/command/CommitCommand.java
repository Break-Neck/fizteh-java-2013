package ru.fizteh.fivt.students.kocurba.filemap.command;

import java.io.IOException;

import ru.fizteh.fivt.students.kocurba.shell.StateWrap;
import ru.fizteh.fivt.students.kocurba.shell.command.Command;

public class CommitCommand implements Command<State> {

    @Override
    public int getArgCount() {
        return 0;
    }

    @Override
    public String getCommandName() {
        return "commit";
    }

    @Override
    public void executeCommand(StateWrap<State> state, String[] arguments)
            throws IOException {
        if (state.getState().getCurrentTable() == null) {
            System.err.println("no table");
            return;
        }

        System.out.println(state.getState().getCurrentTable().commit());


    }

}
