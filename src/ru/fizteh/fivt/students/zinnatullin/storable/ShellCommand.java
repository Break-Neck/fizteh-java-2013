package ru.fizteh.fivt.students.zinnatullin.storable;

public interface ShellCommand {

	public abstract boolean execute(String args[]);

	public abstract String getName();
}