package ru.fizteh.fivt.students.krivchansky.filemap;

import ru.fizteh.fivt.students.krivchansky.shell.SomeCommand;
import ru.fizteh.fivt.students.krivchansky.shell.SomethingIsWrongException;

public class ExitCommand<State extends FileMapShellStateInterface> extends SomeCommand<State>{

	public String getCommandName() {
		return "exit";
	}

	public int getArgumentQuantity() {
		return 0;
	}

	public void implement(String args, State state)
			throws SomethingIsWrongException {
		Table temp = (Table) state.getTable();
		if (temp != null && !temp.getAutoCommit()) {
			state.rollback();
		} else if (temp.getAutoCommit()) {
			state.commit();
		}
		throw new SomethingIsWrongException("EXIT");
	}

}
