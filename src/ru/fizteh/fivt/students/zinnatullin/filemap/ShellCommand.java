package ru.fizteh.fivt.students.zinnatullin.filemap;

interface ShellCommand {
   
    boolean execute(String[] args);
    String getName();
}