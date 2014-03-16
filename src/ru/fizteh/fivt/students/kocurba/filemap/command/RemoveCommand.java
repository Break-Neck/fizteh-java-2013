package ru.fizteh.fivt.students.kocurba.filemap.command;

import java.io.IOException;

import ru.fizteh.fivt.students.kocurba.shell.StateWrap;
import ru.fizteh.fivt.students.kocurba.shell.command.Command;

public class RemoveCommand implements Command<State> {

    @Override
    public int getArgCount() {
        return 1;
    }

    @Override
    public String getCommandName() {
        return "remove";
    }

    @Override
    public void executeCommand(StateWrap<State> state, String[] arguments) throws IOException {
        if (state.getState().getCurrentTable() == null) {
            System.err.println("no table");
            return;
        }
        if (state.getState().getCurrentTable().remove(arguments[1]) != null) {
            System.out.println("removed");
        } else {
            System.out.println("not found");
        }
    }

}
