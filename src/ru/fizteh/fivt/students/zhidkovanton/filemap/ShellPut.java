package ru.fizteh.fivt.students.zhidkovanton.filemap;

import ru.fizteh.fivt.students.zhidkovanton.shell.BaseShellCommand;
import ru.fizteh.fivt.students.zhidkovanton.shell.Command;
import ru.fizteh.fivt.students.zhidkovanton.shell.InvalidCommandException;

public final class ShellPut extends BaseShellCommand {
    private State state;

    public ShellPut(State state) {
        setName("put");
        setNumberOfArgs(3);
        setHint("usage: put <key> <value>");
        this.state = state;
    }

    @Override
    public void execute() {
        String oldValue = state.put(getArg(1), getArg(2));
        if (oldValue == null) {
            System.out.println("new");
        } else {
            System.out.println("overwrite");
            System.out.println(oldValue);
        }
    }

    @Override
    public boolean isAvaliableCommand(final Command command) {
        if (name.equals(command.getArg(0))) {
            if (command.length() < numberOfArgs) {
                throw new InvalidCommandException(name + " " + hint);
            }
            args = command;
            return true;
        }
        return false;
    }
}
