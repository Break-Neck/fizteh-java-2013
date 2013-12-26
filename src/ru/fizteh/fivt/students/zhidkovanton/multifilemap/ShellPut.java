package ru.fizteh.fivt.students.zhidkovanton.multifilemap;

import ru.fizteh.fivt.students.zhidkovanton.shell.BaseShellCommand;
import ru.fizteh.fivt.students.zhidkovanton.shell.Command;
import ru.fizteh.fivt.students.zhidkovanton.shell.InvalidCommandException;

public final class ShellPut extends BaseShellCommand {
    private DataBaseFactory dataBaseFactory;

    public ShellPut(DataBaseFactory dataBaseFactory) {
        setName("put");
        setNumberOfArgs(3);
        setHint("usage: put <key> <value>");
        this.dataBaseFactory = dataBaseFactory;
    }

    @Override
    public void execute() {
        if (dataBaseFactory.dataBase == null) {
            throw new InvalidCommandException("no table");
        }
        String oldValue = dataBaseFactory.dataBase.put(getArg(1), getArg(2));
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
