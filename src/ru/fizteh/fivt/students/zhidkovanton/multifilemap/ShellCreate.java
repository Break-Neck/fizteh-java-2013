package ru.fizteh.fivt.students.zhidkovanton.multifilemap;

import ru.fizteh.fivt.students.zhidkovanton.shell.BaseShellCommand;

public final class ShellCreate extends BaseShellCommand {
    private DataBaseFactory dataBaseFactory;

    public ShellCreate(final DataBaseFactory dataBaseFactory) {
        setName("create");
        setNumberOfArgs(2);
        setHint("usage: create <table name>");
        this.dataBaseFactory = dataBaseFactory;
    }

    @Override
    public void execute() {
        DataBase oldValue = dataBaseFactory.dataFactory.createTable(getArg(1));
        if (oldValue != null) {
            System.out.println("created");
        } else {
            System.out.println(getArg(1) + " exists");
        }
    }
}
