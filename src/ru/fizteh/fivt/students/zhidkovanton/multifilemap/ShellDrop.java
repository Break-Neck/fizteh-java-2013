package ru.fizteh.fivt.students.zhidkovanton.multifilemap;

import ru.fizteh.fivt.students.zhidkovanton.shell.BaseShellCommand;

public final class ShellDrop extends BaseShellCommand {
    private DataBaseFactory dataBaseFactory;

    public ShellDrop(final DataBaseFactory dataBaseFactory) {
        setName("drop");
        setNumberOfArgs(2);
        setHint("usage: drop <table name>");
        this.dataBaseFactory = dataBaseFactory;
    }

    @Override
    public void execute() {
        if (dataBaseFactory.dataFactory.isExists(getArg(1))) {
            if (getArg(1).equals(dataBaseFactory.dataBase.getName())) {
                dataBaseFactory.dataBase = null;
            }
            dataBaseFactory.dataFactory.removeTable(getArg(1));
            System.out.println("dropped");
        } else {
            System.out.println(getArg(1) + " not exists");
        }
    }
}
