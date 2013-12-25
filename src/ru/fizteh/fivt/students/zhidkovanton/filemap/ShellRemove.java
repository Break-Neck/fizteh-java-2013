package ru.fizteh.fivt.students.zhidkovanton.filemap;

import ru.fizteh.fivt.students.zhidkovanton.shell.BaseShellCommand;

public class ShellRemove extends BaseShellCommand {
    private State state;

    public ShellRemove(State state) {
        setName("remove");
        setNumberOfArgs(2);
        setHint("usage: remove <key>");
        this.state = state;
    }

    @Override
    public void execute() {
        String oldValue = state.remove(getArg(1));
        if (oldValue == null) {
            System.out.println("not found");
        } else {
            System.out.println("removed");
        }
    }
}
