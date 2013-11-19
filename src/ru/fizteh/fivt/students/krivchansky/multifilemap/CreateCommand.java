package ru.fizteh.fivt.students.krivchansky.multifilemap;
import ru.fizteh.fivt.students.krivchansky.shell.*;
import ru.fizteh.fivt.students.krivchansky.filemap.*;

public class CreateCommand<Table, Key, Value, State extends MultifileMapShellStateInterface<Table, Key, Value>> extends SomeCommand<State> {
	public String getCommandName() {
		return "create";
	}
	
	public int getArgumentQuantity() {
		return 1;
	}
	
	public void implement(String args, State state) throws SomethingIsWrongException {
		Table newOne = null;
		String tableName = GlobalUtils.parseTableName(args);
		try {
			newOne = state.createTable(args);
			if (newOne != null) {
				System.out.println("created");
			} else {
				System.out.println(tableName + "exists");
			}
		} catch (IllegalArgumentException e) {
			throw new SomethingIsWrongException (e.getMessage());
		}
		
	}

}
