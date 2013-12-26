package ru.fizteh.fivt.students.zhidkovanton.storable;

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
        int numberOfChanges = 0;
        if (dataBaseFactory.dataBase != null) {
            DataBase dataBase = (DataBase) dataBaseFactory.dataBase;
            numberOfChanges = dataBase.getNumberOfChanges();
        }
        if (numberOfChanges != 0) {
            System.out.println(numberOfChanges + " unsaved changes");
        } else {
            DataFactory dataFactory = (DataFactory) dataBaseFactory.dataFactory;

            if (!dataFactory.isExists(getArg(1))) {
                System.out.println(getArg(1) + " not exists");
            } else {
                dataBaseFactory.dataBase = (DataBase) dataBaseFactory.dataFactory.getTable(getArg(1));
                DataBase dataBase = (DataBase) dataBaseFactory.dataBase;
                if (dataBase != null) {
                    dataBase.read();
                }
                System.out.println("using " + getArg(1));
            }
        }
    }
}
