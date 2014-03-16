package ru.fizteh.fivt.students.kocurba.multifilehashmap.command;

import ru.fizteh.fivt.students.kocurba.filemap.command.State;
import ru.fizteh.fivt.students.kocurba.shell.StateWrap;
import ru.fizteh.fivt.students.kocurba.shell.command.Command;

import java.io.IOException;

public class NewExitCommand implements Command<State> {

    @Override
    public int getArgCount() {
        return 0;
    }

    @Override
    public String getCommandName() {
        return "exit";
    }

    @Override
    public void executeCommand(StateWrap<State> state, String[] arguments)
            throws IOException {
        if (state.getState().getCurrentTable() != null) {
            state.getState().getCurrentTable().commit();
        }
        System.out.println("exit");
        System.exit(0);
    }

}
