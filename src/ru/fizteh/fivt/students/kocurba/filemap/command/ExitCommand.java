package ru.fizteh.fivt.students.kocurba.filemap.command;

import java.io.IOException;

import ru.fizteh.fivt.students.kocurba.shell.StateWrap;
import ru.fizteh.fivt.students.kocurba.shell.command.Command;

public class ExitCommand implements Command<State> {

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
