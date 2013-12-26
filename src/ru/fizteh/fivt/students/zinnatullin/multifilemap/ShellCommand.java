package ru.fizteh.fivt.students.zinnatullin.multifilemap;

interface ShellCommand {

	public abstract boolean execute(String args[]);
	public abstract String getName();
}