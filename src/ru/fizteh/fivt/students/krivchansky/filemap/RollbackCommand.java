package ru.fizteh.fivt.students.krivchansky.filemap;


public class RollbackCommand implements Commands {
    
    public String getCommandName() {
        return "rollback";
    }

    public int getArgumentQuantity() {
        return 1;
    }

    public void implement(String[] args, Shell.ShellState state)
            throws SomethingIsWrongException {
        if (state.table == null) {
            throw new SomethingIsWrongException ("No table chosen");
        }
        state.table.rollback();
    }

}
