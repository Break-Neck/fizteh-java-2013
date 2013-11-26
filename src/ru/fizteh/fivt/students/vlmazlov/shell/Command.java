package ru.fizteh.fivt.students.vlmazlov.shell;

import java.io.OutputStream;

public interface Command<T> {
	public String getName();
	public int getArgNum();
	public void execute(String[] args, T state, OutputStream out) throws CommandFailException, UserInterruptionException;

}