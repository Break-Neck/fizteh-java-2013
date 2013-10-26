package ru.fizteh.fivt.students.krivchansky.filemap;

import ru.fizteh.fivt.students.krivchansky.shell.*;

public class ExitCommand implements Commands<FileMapShellState>{

	public String getCommandName() {
		return "exit";
	}

	public int getArgumentQuantity() {
		return 0;
	}

	public void implement(String[] args, FileMapShellState state)
			throws SomethingIsWrongException {
		if (state.table != null) {
			state.table.commit();
		}
		throw new SomethingIsWrongException("EXIT");
	}

}
