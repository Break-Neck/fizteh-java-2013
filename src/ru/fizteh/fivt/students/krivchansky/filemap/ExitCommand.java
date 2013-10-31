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
		if (state.table != null && state.table.getAutoCommit() == true) {
			state.table.commit();
		} else if (state.table != null && state.table.getAutoCommit() == false && state.table.getChangesCounter() > 0) {
			throw new SomethingIsWrongException (state.table.getChangesCounter() + " unsaved changes");
		}
		throw new SomethingIsWrongException("EXIT");
	}

}
