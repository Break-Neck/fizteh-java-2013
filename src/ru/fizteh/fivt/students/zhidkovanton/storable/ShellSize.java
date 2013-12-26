package ru.fizteh.fivt.students.zhidkovanton.storable;

import ru.fizteh.fivt.students.zhidkovanton.shell.BaseShellCommand;

public class ShellSize extends BaseShellCommand {
    private DataBaseFactory dataBaseFactory;

    public ShellSize(final DataBaseFactory dataBaseFactory) {
        setName("size");
        setNumberOfArgs(1);
        setHint("usage: size");
        this.dataBaseFactory = dataBaseFactory;
    }

    @Override
    public void execute() {
        if (dataBaseFactory.dataBase != null) {
            System.out.println(dataBaseFactory.dataBase.size());
        } else {
            System.out.println("no table");
        }
    }

}
