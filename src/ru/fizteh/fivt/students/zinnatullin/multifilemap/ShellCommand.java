package ru.fizteh.fivt.students.zinnatullin.multifilemap;

interface ShellCommand {

    abstract boolean execute(String[] args);
    abstract String getName();
}