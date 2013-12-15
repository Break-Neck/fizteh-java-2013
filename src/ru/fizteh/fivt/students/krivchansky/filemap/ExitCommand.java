package src.ru.fizteh.fivt.students.krivchansky.filemap;

import src.ru.fizteh.fivt.students.krivchansky.shell.*;

public class ExitCommand<State extends FileMapShellStateInterface> extends SomeCommand<State>{

	public String getCommandName() {
		return "exit";
	}

	public int getArgumentQuantity() {
		return 0;
	}

	public void implement(String args, State state)
			throws SomethingIsWrongException {
		if (state.getTable() != null) {
			state.rollback();
		}
		throw new SomethingIsWrongException("EXIT");
	}

}
