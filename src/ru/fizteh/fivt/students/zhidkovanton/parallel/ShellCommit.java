package ru.fizteh.fivt.students.zhidkovanton.parallel;

import ru.fizteh.fivt.students.zhidkovanton.shell.BaseShellCommand;

import java.io.IOException;

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
        try {
            if (dataBaseFactory.dataBase != null) {
                System.out.println(dataBaseFactory.dataBase.commit());
            } else {
                System.out.println("no table");
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
