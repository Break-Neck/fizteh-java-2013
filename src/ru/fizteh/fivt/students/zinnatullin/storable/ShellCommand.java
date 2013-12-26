package ru.fizteh.fivt.students.zinnatullin.storable.commands;

public interface ShellCommand {

	public abstract boolean execute(String args[]);

	public abstract String getName();
}