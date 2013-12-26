package ru.fizteh.fivt.students.zinnatullin.junit;

interface ShellCommand {

	public abstract boolean execute(String[] args);

	public abstract String getName();
}