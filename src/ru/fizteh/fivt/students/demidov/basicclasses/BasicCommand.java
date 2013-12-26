package ru.fizteh.fivt.students.demidov.basicclasses;

import java.io.IOException;

import ru.fizteh.fivt.students.demidov.shell.Shell;
import ru.fizteh.fivt.students.demidov.shell.ShellInterruptionException;

public interface BasicCommand {
	void executeCommand(String[] arguments, Shell usedShell) throws IOException, ShellInterruptionException;
	int getNumberOfArguments();
	String getCommandName();
}
