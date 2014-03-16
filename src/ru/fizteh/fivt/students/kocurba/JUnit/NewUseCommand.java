package ru.fizteh.fivt.students.kocurba.JUnit;

import ru.fizteh.fivt.students.kocurba.filemap.command.State;
import ru.fizteh.fivt.students.kocurba.shell.StateWrap;
import ru.fizteh.fivt.students.kocurba.shell.command.Command;
import ru.fizteh.fivt.students.kocurba.storeable.StoreableTable;

import java.io.IOException;

public class NewUseCommand implements Command<State> {

    @Override
    public int getArgCount() {
        return 1;
    }

    @Override
    public String getCommandName() {
        return "use";
    }

    @Override
    public void executeCommand(StateWrap<State> state, String[] arguments)
            throws IOException {
        if (state.getState().getCurrentTable() != null
                && ((StoreableTable) state.getState().getCurrentTable()).getNumberOfChanges() > 0) {
            System.err.println(((StoreableTable) state.getState().getCurrentTable()).getNumberOfChanges()
                    + " unsaved changes");
            //state.getState().getCurrentTable().commit();
            return;
        }

        StoreableTable newTable = (StoreableTable) state.getState().getTableProvider().getTable(arguments[1]);
        if (newTable == null) {
            System.err.println(arguments[1] + " not exists");
            return;
        }
        state.getState().setCurrentTable(newTable);
        System.out.println("using " + arguments[1]);
    }
}
