package ru.fizteh.fivt.students.zhidkovanton.storable;

import ru.fizteh.fivt.students.zhidkovanton.shell.BaseShellCommand;

public final class ShellExit extends BaseShellCommand {
    public ShellExit() {
        setName("exit");
        setNumberOfArgs(1);
        setHint("usage: exit");
    }

    @Override
    public void execute() {
        throw new ShellExitException("Exit command");
    }
}
