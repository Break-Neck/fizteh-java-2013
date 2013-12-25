package ru.fizteh.fivt.students.zhidkovanton.JUnit;

import ru.fizteh.fivt.students.zhidkovanton.shell.BaseShellCommand;

public class ShellCommit extends BaseShellCommand {
    private DataBaseFactory dataBaseFactory;

    public ShellCommit(final DataBaseFactory dataBaseFactory) {
        setName("commit");
        setNumberOfArgs(1);
        setHint("usage: commit");
        this.dataBaseFactory = dataBaseFactory;
    }

    @Override
    public void execute() {
        if (dataBaseFactory.dataBase != null) {
            System.out.println(dataBaseFactory.dataBase.commit());
            DataBase dataBase = (DataBase) dataBaseFactory.dataBase;
            dataBase.print();
        } else {
            System.out.println("no table");
        }
    }
}
