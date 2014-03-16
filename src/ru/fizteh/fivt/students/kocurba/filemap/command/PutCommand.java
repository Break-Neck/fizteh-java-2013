package ru.fizteh.fivt.students.kocurba.filemap.command;

import java.io.IOException;

import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.students.kocurba.shell.StateWrap;
import ru.fizteh.fivt.students.kocurba.shell.command.Command;
import java.text.ParseException;

public class PutCommand implements Command<State> {

    @Override
    public int getArgCount() {
        return -1;
    }

    @Override
    public String getCommandName() {
        return "put";
    }

    @Override
    public void executeCommand(StateWrap<State> state, String[] arguments)
            throws IOException {
        if (state.getState().getCurrentTable() == null) {
            System.err.println("no table");
            return;
        }
        try {
            Storeable oldValue = state.getState().getCurrentTable().putStoreable(arguments[1],
                    getSpacedArg(arguments, 2));
            if (oldValue == null) {
                System.out.println("new");
            } else {
                System.out.println("overwrite");
                System.out.println(state.getState().getTableProvider().serialize(state.getState().getCurrentTable(),
                        oldValue));
            }
        } catch (ParseException e) {
            System.out.println("wrong type (" + e.getMessage() + ")");
        }
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
