package ru.fizteh.fivt.students.zinnatullin.shell;

import java.io.File;

import ru.fizteh.fivt.students.zinnatullin.basicclasses.BasicCommand;

public class Dir implements BasicCommand {
	public void executeCommand(String[] arguments, Shell usedShell) {    
		for (String currentFile : (new File(usedShell.curShell.getCurrentDirectory())).list()) {
			System.out.println(currentFile);
		}
	}
	public int getNumberOfArguments() {
		return 0;
	}	
	public String getCommandName() {
		return "dir";
	}
}
