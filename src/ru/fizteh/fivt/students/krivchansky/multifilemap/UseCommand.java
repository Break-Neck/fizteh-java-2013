package ru.fizteh.fivt.students.krivchansky.multifilemap;

import java.io.IOException;

import ru.fizteh.fivt.students.krivchansky.filemap.*;
import ru.fizteh.fivt.students.krivchansky.shell.*;

public class UseCommand implements Commands<MultiFileMapShellState> {
	public String getCommandName() {
		return "use";
	}
	
	public int getArgumentQuantity() {
		return 1;
	}
	
	public void implement(String[] args, MultiFileMapShellState state) throws SomethingIsWrongException {
		MultifileTable oldOne = (MultifileTable) state.table;
		state.table = state.tableProvider.getTable(args[0]);
		System.out.println("using table " + state.table.getName());
		if (oldOne != null) {
			oldOne.commit();
		}
	}
}
