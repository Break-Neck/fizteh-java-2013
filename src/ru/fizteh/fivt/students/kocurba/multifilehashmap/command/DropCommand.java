package ru.fizteh.fivt.students.kocurba.multifilehashmap.command;

import java.io.IOException;

import ru.fizteh.fivt.students.kocurba.filemap.command.State;
import ru.fizteh.fivt.students.kocurba.shell.StateWrap;
import ru.fizteh.fivt.students.kocurba.shell.command.Command;

public class DropCommand implements Command<State> {

    @Override
    public int getArgCount() {
        return 1;
    }

    @Override
    public String getCommandName() {
        return "drop";
    }

    @Override
    public void executeCommand(StateWrap<State> state, String[] arguments) throws IOException {
        if (state.getState().getCurrentTable() != null && state.getState().getCurrentTable().getName()
                        .equals(arguments[1])) {
            state.getState().setCurrentTable(null);
        }
        state.getState().getTableProvider().removeTable(arguments[1]);
        System.out.println("dropped");
    }

}
