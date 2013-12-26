package ru.fizteh.fivt.students.zinnatullin.junit;

interface ShellCommand {

    abstract boolean execute(String[] args);
    abstract String getName();
}