package ru.fizteh.fivt.students.zinnatullin.multifilehashmap;

import java.io.IOException;

import ru.fizteh.fivt.students.zinnatullin.basicclasses.BasicCommand;
import ru.fizteh.fivt.students.zinnatullin.basicclasses.BasicDataBaseState;
import ru.fizteh.fivt.students.zinnatullin.basicclasses.BasicTable;
import ru.fizteh.fivt.students.zinnatullin.shell.Shell;
import ru.fizteh.fivt.students.zinnatullin.shell.ShellInterruptionException;

public abstract class BasicMultiFileHashMapCommand<ElementType, TableType extends BasicTable<ElementType>> implements BasicCommand {
	protected BasicDataBaseState<ElementType, TableType> dataBaseState;
	private String commandName;
	private int argumentsNumber;
	
	public BasicMultiFileHashMapCommand(BasicDataBaseState<ElementType, TableType> dataBaseState, String commandName, int argumentsNumber) {
		this.dataBaseState = dataBaseState;
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
