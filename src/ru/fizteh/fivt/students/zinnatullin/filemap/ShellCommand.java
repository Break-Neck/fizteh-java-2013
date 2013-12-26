package ru.fizteh.fivt.students.zinnatullin.filemap;

interface ShellCommand {
    
    abstract boolean execute(String[] args);
    abstract String getName();
}