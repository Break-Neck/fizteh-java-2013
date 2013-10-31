package ru.fizteh.fivt.students.krivchansky.filemap;
import ru.fizteh.fivt.students.krivchansky.shell.*;

public class RemoveKeyCommand implements Commands<FileMapShellState> {

    public String getCommandName() {
        return "remove";
    }

    public int getArgumentQuantity() {
        return 1;
    }

    public void implement(String[] args, FileMapShellState state)
            throws SomethingIsWrongException {
    	if (state.table == null) {
    		throw new SomethingIsWrongException("no table");
    	}
        String a = state.table.remove(args[0]);
        if (a == null || a.isEmpty()) {
            System.out.println("not found");
        } else {
            System.out.println("removed");
        }
    }

}
