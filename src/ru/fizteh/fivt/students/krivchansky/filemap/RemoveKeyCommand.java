package ru.fizteh.fivt.students.krivchansky.filemap;
import java.util.ArrayList;

import ru.fizteh.fivt.students.krivchansky.shell.SomeCommand;
import ru.fizteh.fivt.students.krivchansky.shell.SomethingIsWrongException;
import ru.fizteh.fivt.students.krivchansky.shell.Parser;

public class RemoveKeyCommand<Table, Key, Value, State extends FileMapShellStateInterface<Table, Key, Value>> extends SomeCommand<State>{

    public String getCommandName() {
        return "remove";
    }

    public int getArgumentQuantity() {
        return 1;
    }
    
    public void implement(String args, State state)
            throws SomethingIsWrongException {
    	if (state.getTable() == null) {
    		throw new SomethingIsWrongException("no table");
    	}
    	ArrayList<String> parameters = Parser.parseCommandArgs(args);
        if (parameters.size() != 1) {
            throw new IllegalArgumentException("Use 1 argument for this operation");
        }
    	Key key = state.parseKey(parameters.get(0));
        Value a =  state.remove(key);
        if (a == null) {
            System.out.println("not found");
        } else {
            System.out.println("removed");
        }
    }

}
