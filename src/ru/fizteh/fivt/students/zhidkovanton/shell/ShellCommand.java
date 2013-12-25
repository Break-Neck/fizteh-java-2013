package ru.fizteh.fivt.students.zhidkovanton.shell;

public interface ShellCommand {
    void execute();

    boolean isAvaliableCommand(Command command);
}
