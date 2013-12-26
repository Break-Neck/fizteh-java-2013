package ru.fizteh.fivt.students.krivchansky.multifilemap;


import java.util.ArrayList;

import ru.fizteh.fivt.students.krivchansky.shell.Parser;
import ru.fizteh.fivt.students.krivchansky.shell.SomeCommand;
import ru.fizteh.fivt.students.krivchansky.shell.SomethingIsWrongException;

public class UseCommand<Table, Key, Value, State extends MultifileMapShellStateInterface<Table, Key, Value>> extends SomeCommand<State> {
	public String getCommandName() {
		return "use";
	}
	
	public int getArgumentQuantity() {
		return 1;
	}
	
	public void implement(String args, State state) throws SomethingIsWrongException {
		ArrayList<String> parameters = Parser.parseCommandArgs(args);
        if (parameters.size() != 1) {
            throw new IllegalArgumentException("Correct number of arguments -- 1");
        }
		Table newOne = null;
		try {
			newOne = state.useTable(parameters.get(0));
		} catch (IllegalStateException e) {
			System.err.println(e.getMessage());
            return;
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            return;
        }
		
		if (newOne == null) {
			System.out.println(parameters.get(0) + " not exists");
            return;
        }

        System.out.println("using " + state.getCurrentTableName());
		
	}
}