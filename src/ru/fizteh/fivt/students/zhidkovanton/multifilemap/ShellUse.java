package ru.fizteh.fivt.students.zhidkovanton.multifilemap;

import ru.fizteh.fivt.students.zhidkovanton.shell.BaseShellCommand;

public final class ShellUse extends BaseShellCommand {
    private DataBaseFactory dataBaseFactory;

    public ShellUse(final DataBaseFactory dataBaseFactory) {
        setName("use");
        setNumberOfArgs(2);
        setHint("usage: use <table name>");
        this.dataBaseFactory = dataBaseFactory;
    }

    @Override
    public void execute() {
        if (dataBaseFactory.dataBase != null) {
            dataBaseFactory.dataBase.print();
        }
        if (!dataBaseFactory.dataFactory.isExists(getArg(1))) {
            System.out.println(getArg(1) + " not exists");
        } else {
            System.out.println("using " + getArg(1));
            dataBaseFactory.dataBase = dataBaseFactory.dataFactory.getTable(getArg(1));
            dataBaseFactory.dataBase.read();
        }
    }
}
