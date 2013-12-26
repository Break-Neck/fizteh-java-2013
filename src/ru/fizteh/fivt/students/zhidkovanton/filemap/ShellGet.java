package ru.fizteh.fivt.students.zhidkovanton.filemap;

import ru.fizteh.fivt.students.zhidkovanton.shell.BaseShellCommand;

public final class ShellGet extends BaseShellCommand {
    private State state;

    public ShellGet(State state) {
        setName("get");
        setNumberOfArgs(2);
        setHint("usage: get <key>");
        this.state = state;
    }

    @Override
    public void execute() {
        String oldValue = state.get(getArg(1));
        if (oldValue == null) {
            System.out.println("not found");
        } else {
            System.out.println("found");
            System.out.println(oldValue);
        }
    }
}
