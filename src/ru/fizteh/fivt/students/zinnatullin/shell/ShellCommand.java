package ru.fizteh.fivt.students.zinnatullin.shell;

interface ShellCommand {
    abstract boolean execute(String[] args);
}