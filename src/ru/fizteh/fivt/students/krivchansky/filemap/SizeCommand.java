package ru.fizteh.fivt.students.krivchansky.filemap;


import src.ru.fizteh.fivt.students.krivchansky.shell.SomeCommand;
import src.ru.fizteh.fivt.students.krivchansky.shell.SomethingIsWrongException;

public class SizeCommand<Table, Key, Value, State extends FileMapShellStateInterface<Table, Key, Value>> extends SomeCommand<State> {

	public String getCommandName() {
		return "size";
	}

	public int getArgumentQuantity() {
		return 0;
	}

	public void implement(String args, State state)
			throws SomethingIsWrongException {
		if (state.getTable() == null) {
			throw new SomethingIsWrongException("no table");
		}
		System.out.println(state.size());
	}

}
