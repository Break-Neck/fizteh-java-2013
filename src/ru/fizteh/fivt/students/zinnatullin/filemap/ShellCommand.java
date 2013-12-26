package ru.fizteh.fivt.students.zinnatullin.filemap;

interface ShellCommand {
    
    public abstract boolean execute(String[] args);
    public abstract String getName();
}