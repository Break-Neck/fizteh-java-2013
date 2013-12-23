package ru.fizteh.fivt.students.zhidkovanton.multifilemap;

import ru.fizteh.fivt.students.zhidkovanton.shell.BaseShellCommand;
import ru.fizteh.fivt.students.zhidkovanton.shell.Command;
import ru.fizteh.fivt.students.zhidkovanton.shell.InvalidCommandException;


public final class ShellGet extends BaseShellCommand {
    private DataBaseFactory dataBaseFactory;

    public ShellGet(DataBaseFactory dataBaseFactory) {
        setName("get");
        setNumberOfArgs(2);
        setHint("usage: get <key>");
        this.dataBaseFactory = dataBaseFactory;
    }

    @Override
    public void execute() {
        if (dataBaseFactory.dataBase == null) {
            throw new InvalidCommandException("no table");
        }
        String oldValue = dataBaseFactory.dataBase.get(getArg(1));
        if (oldValue == null) {
            System.out.println("not found");
        } else {
            System.out.println("found");
            System.out.println(oldValue);
        }
    }
}
