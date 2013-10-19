import ru.fizteh.fivt.students.krivchansky.shell;
package ru.fizteh.fivt.students.krivchansky.filemap;

public class CommitCommand implements Commands{
    
    public String getCommandName() {
        return "commit";
    }

    public int getArgumentQuantity() {
        return 0;
    }

    public void implement(String[] args, Shell.ShellState state)
            throws SomethingIsWrongException {
        if (state.table == null) {
            throw new SomethingIsWrongException ("Table not found.");
        }
        state.table.commit();
    }

}
