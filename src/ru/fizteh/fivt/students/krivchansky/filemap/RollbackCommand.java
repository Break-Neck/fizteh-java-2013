package ru.fizteh.fivt.students.krivchansky.filemap;
import ru.fizteh.fivt.students.krivchansky.shell.*;

public class RollbackCommand implements Commands<FileMapShellState> {
    
    public String getCommandName() {
        return "rollback";
    }

    public int getArgumentQuantity() {
        return 0;
    }

    public void implement(String[] args, FileMapShellState state)
            throws SomethingIsWrongException {
        if (state.table == null) {
        	throw new SomethingIsWrongException("no table");
        }
        int temp = state.table.getChangesCounter();
        state.table.rollback();
        System.out.println(temp);
    }

}
