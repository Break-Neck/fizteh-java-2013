package ru.fizteh.fivt.students.zhidkovanton.storable;

import ru.fizteh.fivt.students.zhidkovanton.shell.BaseShellCommand;

import java.io.IOException;

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
        try {
            DataFactory dataFactory = (DataFactory) dataBaseFactory.dataFactory;
            if (dataBaseFactory.dataBase != null) {
                if (getArg(1).equals(dataBaseFactory.dataBase.getName())) {
                    dataBaseFactory.dataBase = null;
                }
            }
            dataBaseFactory.dataFactory.removeTable(getArg(1));
            System.out.println("dropped");
        } catch (IllegalStateException e) {
            System.out.println(getArg(1) + " not exists");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
