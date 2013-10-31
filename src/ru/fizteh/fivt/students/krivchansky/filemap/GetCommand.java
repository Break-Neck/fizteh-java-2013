package ru.fizteh.fivt.students.krivchansky.filemap;
import ru.fizteh.fivt.students.krivchansky.shell.*;

public class GetCommand implements Commands<FileMapShellState>{

    public String getCommandName() {
        return "get";
    }

    public int getArgumentQuantity() {
        return 1;
    }

    public void implement(String[] args, FileMapShellState state)
            throws SomethingIsWrongException {
        if (state.table == null) {
            throw new SomethingIsWrongException ("no table");
        }
        String value = state.table.get(args[0]);
        if (value == null) {
            System.out.println("not found");
        } else {
            System.out.println("found\n" + value);
        }
        
    }
    

}
