package ru.fizteh.fivt.students.krivchansky.filemap;
import ru.fizteh.fivt.students.krivchansky.shell.*;

public class CommitCommand implements Commands<FileMapShellState>{
    
    public String getCommandName() {
        return "commit";
    }

    public int getArgumentQuantity() {
        return 0;
    }

    public void implement(String[] args, FileMapShellState state)
            throws SomethingIsWrongException {
        if (state.table == null) {
            throw new SomethingIsWrongException ("no table");
        }
        int temp = state.table.getChangesCounter();
        state.table.commit();
        System.out.println(temp);
    }

}
