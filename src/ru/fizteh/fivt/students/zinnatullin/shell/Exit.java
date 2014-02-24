package ru.fizteh.fivt.students.zinnatullin.shell;

import ru.fizteh.fivt.students.zinnatullin.basicclasses.BasicCommand;
import ru.fizteh.fivt.students.zinnatullin.shell.Shell;

public class Exit implements BasicCommand {
	public void executeCommand(String[] arguments, Shell usedShell) throws ShellInterruptionException {    
		throw new ShellInterruptionException();
	}	
	public int getNumberOfArguments() {
		return 0;
	}	
	public String getCommandName() {
		return "exit";
	}
}
