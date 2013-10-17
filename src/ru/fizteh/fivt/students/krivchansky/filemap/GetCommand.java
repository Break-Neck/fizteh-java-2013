package ru.fizteh.fivt.students.krivchansky.filemap;


public class GetCommand implements Commands {

    public String getCommandName() {
        return "get";
    }

    public int getArgumentQuantity() {
        return 1;
    }

    public void implement(String[] args, Shell.ShellState state)
            throws SomethingIsWrongException {
        if(state.table == null) {
            throw new SomethingIsWrongException ("Table not found.");
        }
        String value = state.table.get(args[0]);
        if (value == null) {
            System.out.println("Not found\n");
        } else {
            System.out.println("Found:\n" + value);
        }
        
    }
    

}
