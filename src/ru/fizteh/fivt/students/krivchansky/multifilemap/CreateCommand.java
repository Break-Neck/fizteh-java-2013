package ru.fizteh.fivt.students.krivchansky.multifilemap;
import ru.fizteh.fivt.students.krivchansky.shell.*;
import ru.fizteh.fivt.students.krivchansky.filemap.*;

public class CreateCommand implements Commands<MultiFileMapShellState> {
	public String getCommandName() {
		return "create";
	}
	
	public int getArgumentQuantity() {
		return 1;
	}
	
	public void implement(String[] args, MultiFileMapShellState state) throws SomethingIsWrongException {
		try {
			state.tableProvider.createTable(args[0]);
			System.out.println("created");
		} catch (Exception e) {
			throw new SomethingIsWrongException (e.getMessage());
		}
	}

}
