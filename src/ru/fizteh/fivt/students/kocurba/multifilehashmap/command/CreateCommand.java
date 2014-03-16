package ru.fizteh.fivt.students.kocurba.multifilehashmap.command;

import java.io.IOException;

import ru.fizteh.fivt.storage.structured.Table;
import ru.fizteh.fivt.students.kocurba.filemap.command.State;
import ru.fizteh.fivt.students.kocurba.shell.StateWrap;
import ru.fizteh.fivt.students.kocurba.shell.command.Command;
import ru.fizteh.fivt.students.kocurba.storeable.MySignature;

public class CreateCommand implements Command<State> {

    @Override
    public int getArgCount() {
        return -1;
    }

    @Override
    public String getCommandName() {
        return "create";
    }

    @Override
    public void executeCommand(StateWrap<State> state, String[] arguments)
            throws IOException {
        Table newTable = state.getState().getTableProvider().createTable(arguments[1],
                MySignature.getTypes(getSpacedArg(arguments, 2)));
        if (newTable == null) {
            System.err.println(arguments[1] + " exists");
            return;
        }
        System.out.println("created");
    }
    public String getSpacedArg(String[] argList, final int index) {
        String initialCommand = new String("");

        for (int i = index; i < argList.length; ++i) {
            initialCommand += " " + argList[i];
        }
        initialCommand = initialCommand.trim();
        return initialCommand;
    }
}
