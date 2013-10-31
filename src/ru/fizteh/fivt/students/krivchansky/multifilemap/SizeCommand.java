package ru.fizteh.fivt.students.krivchansky.multifilemap;

import ru.fizteh.fivt.students.krivchansky.shell.Commands;
import ru.fizteh.fivt.students.krivchansky.shell.SomethingIsWrongException;

public class SizeCommand implements Commands<MultiFileMapShellState> {

	public String getCommandName() {
		return "size";
	}

	public int getArgumentQuantity() {
		return 0;
	}

	public void implement(String[] args, MultiFileMapShellState state)
			throws SomethingIsWrongException {
		if (state.table == null) {
			throw new SomethingIsWrongException("no table");
		}
		System.out.println(state.table.size());
	}

}
