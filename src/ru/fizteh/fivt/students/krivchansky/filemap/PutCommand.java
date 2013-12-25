package ru.fizteh.fivt.students.krivchansky.filemap;
import java.util.ArrayList;

import ru.fizteh.fivt.students.krivchansky.shell.SomeCommand;
import ru.fizteh.fivt.students.krivchansky.shell.SomethingIsWrongException;
import ru.fizteh.fivt.students.krivchansky.shell.Parser;

public class PutCommand<Table, Key, Value, State extends FileMapShellStateInterface<Table, Key, Value>> extends SomeCommand<State>{

    public String getCommandName() {
        return "put";
    }

    public int getArgumentQuantity() {
        return 2;
    }

    public void implement(String args, State state)
            throws SomethingIsWrongException {
    	if (state.getTable() == null) {
    		System.err.println("no table");
    	}
    	ArrayList<String> parameters = Parser.parseCommandArgs(args);
        if (parameters.size() != 2) {
            throw new IllegalArgumentException("Use 2 arguments for this operation");
        }
    	Key key = state.parseKey(parameters.get(0));
    	Value value = state.parseValue(parameters.get(1));
    	Value temp =  state.put(key, value);
        if (temp != null) {
                System.out.println("overwrite\n" + state.valueToString(temp));
        } else {
            System.out.println("new");
        }
    }

}
