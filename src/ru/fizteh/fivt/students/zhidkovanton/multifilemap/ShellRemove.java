package ru.fizteh.fivt.students.zhidkovanton.multifilemap;

import ru.fizteh.fivt.students.zhidkovanton.shell.BaseShellCommand;
import ru.fizteh.fivt.students.zhidkovanton.shell.InvalidCommandException;

public class ShellRemove extends BaseShellCommand {
    private DataBaseFactory dataBaseFactory;

    public ShellRemove(DataBaseFactory dataBaseFactory) {
        setName("remove");
        setNumberOfArgs(2);
        setHint("usage: remove <key>");
        this.dataBaseFactory = dataBaseFactory;
    }

    @Override
    public void execute() {
        if (dataBaseFactory.dataBase == null) {
            throw new InvalidCommandException("no table");
        }
        String oldValue = dataBaseFactory.dataBase.remove(getArg(1));
        if (oldValue == null) {
            System.out.println("not found");
        } else {
            System.out.println("removed");
        }
    }
}
