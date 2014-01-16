package ru.fizteh.fivt.students.zinnatullin.filemap;

import java.io.IOException;

import ru.fizteh.fivt.students.zinnatullin.basicclasses.BasicState;
import ru.fizteh.fivt.students.zinnatullin.shell.Shell;
import ru.fizteh.fivt.students.zinnatullin.storeable.WrongTypeException;

public class Get extends BasicFileMapCommand {
	public Get(BasicState currentState) {
		super(currentState, "get", 1);
	}	
	public void executeCommand(String[] arguments, Shell usedShell) throws IOException { 
		String value = null;
		try {
			value = currentState.get(arguments[0]);
		} catch (WrongTypeException catchedException) {
			throw new IOException("wrong type (" + catchedException.getMessage() + ")");
		}
		
		if (value == null) {
			usedShell.curShell.getOutStream().println("not found");
		} else {
			usedShell.curShell.getOutStream().println("found\n" + value);
		}
	}	
}
