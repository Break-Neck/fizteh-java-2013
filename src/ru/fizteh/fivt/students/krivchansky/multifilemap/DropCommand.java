package src.ru.fizteh.fivt.students.krivchansky.multifilemap;
import java.io.IOException;
import java.util.ArrayList;

import src.ru.fizteh.fivt.students.krivchansky.shell.Parser;
import src.ru.fizteh.fivt.students.krivchansky.shell.SomeCommand;
import src.ru.fizteh.fivt.students.krivchansky.shell.SomethingIsWrongException;


public class DropCommand<State extends MultifileMapShellStateInterface> extends SomeCommand<State>{
	public String getCommandName() {
		return "drop";
	}
	
	public int getArgumentQuantity() {
		return 1;
	}
	
	public void implement(String args, State state) throws SomethingIsWrongException {
		ArrayList<String> parameters = Parser.parseCommandArgs(args);
		if (parameters.size() != 1) {
            throw new IllegalArgumentException("Correct number of arguments -- 1");
        }
		try {
			state.dropTable(parameters.get(0));
			System.out.println("dropped");
		} catch (IOException e) {
			throw new SomethingIsWrongException (e.getMessage());
		} catch (IllegalStateException e) {
			throw new SomethingIsWrongException (e.getMessage());
		}
	}

}