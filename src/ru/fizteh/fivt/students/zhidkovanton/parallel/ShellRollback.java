package ru.fizteh.fivt.students.zhidkovanton.parallel;

import ru.fizteh.fivt.students.zhidkovanton.shell.BaseShellCommand;

public class ShellRollback extends BaseShellCommand {
    private DataBaseFactory dataBaseFactory;

    public ShellRollback(final DataBaseFactory dataBaseFactory) {
        setName("rollback");
        setNumberOfArgs(1);
        setHint("usage: rollback");
        this.dataBaseFactory = dataBaseFactory;
    }

    @Override
    public void execute() {
        if (dataBaseFactory.dataBase != null) {
            System.out.println(dataBaseFactory.dataBase.rollback());
        } else {
            System.out.println("no table");
        }
    }
}
