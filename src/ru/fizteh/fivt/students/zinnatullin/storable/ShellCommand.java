package ru.fizteh.fivt.students.zinnatullin.storable;

interface ShellCommand {

    abstract boolean execute(String[] args);
    abstract String getName();
}