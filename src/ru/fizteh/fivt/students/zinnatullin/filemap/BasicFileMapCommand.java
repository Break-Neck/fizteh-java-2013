package ru.fizteh.fivt.students.zinnatullin.filemap;

import java.io.IOException;

import ru.fizteh.fivt.students.zinnatullin.basicclasses.BasicCommand;
import ru.fizteh.fivt.students.zinnatullin.basicclasses.BasicState;
import ru.fizteh.fivt.students.zinnatullin.shell.Shell;
import ru.fizteh.fivt.students.zinnatullin.shell.ShellInterruptionException;

public abstract class BasicFileMapCommand implements BasicCommand {
	protected BasicState currentState;
	private String commandName;
	private int argumentsNumber;
	
	public BasicFileMapCommand(BasicState currentState, String commandName, int argumentsNumber) {
		this.currentState = currentState;
		this.commandName = commandName;
		this.argumentsNumber = argumentsNumber;
	}
	
	abstract public void executeCommand(String[] arguments, Shell usedShell) throws IOException, ShellInterruptionException;
	
	public int getNumberOfArguments() {
		return argumentsNumber;
	}
	
	public String getCommandName() {
		return commandName;
	}
}
