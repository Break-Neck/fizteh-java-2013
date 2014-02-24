package ru.fizteh.fivt.students.zinnatullin.basicclasses;

import java.io.IOException;

import ru.fizteh.fivt.students.zinnatullin.shell.Shell;
import ru.fizteh.fivt.students.zinnatullin.shell.ShellInterruptionException;

public interface BasicCommand {
	void executeCommand(String[] arguments, Shell usedShell) throws IOException, ShellInterruptionException;
	int getNumberOfArguments();
	String getCommandName();
}
